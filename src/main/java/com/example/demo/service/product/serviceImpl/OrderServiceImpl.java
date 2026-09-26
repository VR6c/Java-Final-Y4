package com.example.demo.service.product.serviceImpl;

import com.example.demo.config.CacheConfig;
import com.example.demo.dto.patch.product.OrderPatchRequest;
import com.example.demo.dto.request.product.OrderRequest;
import com.example.demo.dto.request.product.ProductOrderRequest;
import com.example.demo.dto.response.product.OrderResponse;
import com.example.demo.dto.response.product.ProductOrderResponse;
import com.example.demo.entity.product.Customer;
import com.example.demo.entity.product.Order;
import com.example.demo.entity.product.Product;
import com.example.demo.entity.product.ProductOrder;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.product.CustomerRepository;
import com.example.demo.repository.product.OrderRepository;
import com.example.demo.repository.product.ProductRepository;
import com.example.demo.service.product.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository,
                            ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<OrderResponse> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_ORDERS, key = "#id")
    public OrderResponse getById(Long id) {
        return toResponse(findOrderById(id));
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        Customer customer = findCustomerById(request.customerId());

        Order order = new Order();
        order.setOrderDate(request.orderDate() != null ? request.orderDate() : LocalDate.now().toString());
        order.setStatus(request.status() != null ? request.status() : "PENDING");
        order.setCustomer(customer);

        List<ProductOrder> items = buildProductOrders(request.productOrders(), order);
        order.setProductOrders(items);

        double calculatedTotal = calculateTotalAmount(items);
        order.setTotalAmount(calculatedTotal);

        return toResponse(orderRepository.save(order));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_ORDERS, key = "#id")
    public OrderResponse update(Long id, OrderRequest request) {
        Order existingOrder = findOrderById(id);
        Customer customer = findCustomerById(request.customerId());

        existingOrder.setOrderDate(request.orderDate() != null ? request.orderDate() : existingOrder.getOrderDate());
        existingOrder.setStatus(request.status() != null ? request.status() : existingOrder.getStatus());
        existingOrder.setCustomer(customer);

        existingOrder.getProductOrders().clear();
        List<ProductOrder> newItems = buildProductOrders(request.productOrders(), existingOrder);
        existingOrder.getProductOrders().addAll(newItems);

        double calculatedTotal = calculateTotalAmount(newItems);
        existingOrder.setTotalAmount(calculatedTotal);

        return toResponse(orderRepository.save(existingOrder));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_ORDERS, key = "#id")
    public OrderResponse edit(Long id, OrderPatchRequest request) {
        Order existingOrder = findOrderById(id);

        if (request.orderDate() != null) {
            existingOrder.setOrderDate(request.orderDate());
        }
        if (request.status() != null) {
            existingOrder.setStatus(request.status());
        }

        if (Boolean.TRUE.equals(request.unlinkCustomer())) {
            existingOrder.setCustomer(null);
        } else if (request.customerId() != null) {
            Customer customer = findCustomerById(request.customerId());
            existingOrder.setCustomer(customer);
        }

        if (Boolean.TRUE.equals(request.clearProductOrders())) {
            existingOrder.getProductOrders().clear();
        } else if (request.productOrders() != null) {
            existingOrder.getProductOrders().clear();
            List<ProductOrder> newItems = buildProductOrders(request.productOrders(), existingOrder);
            existingOrder.getProductOrders().addAll(newItems);
        }

        double calculatedTotal = calculateTotalAmount(existingOrder.getProductOrders());
        existingOrder.setTotalAmount(calculatedTotal);

        return toResponse(orderRepository.save(existingOrder));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_ORDERS, key = "#id")
    public void deleteById(Long id) {
        orderRepository.delete(findOrderById(id));
    }

    @Override
    public Page<OrderResponse> search(String status, Long customerId, Pageable pageable) {
        return orderRepository.searchOrders(status, customerId, pageable)
                .map(this::toResponse);
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private List<ProductOrder> buildProductOrders(List<ProductOrderRequest> requests, Order order) {
        List<ProductOrder> productOrders = new ArrayList<>();
        if (requests != null && !requests.isEmpty()) {
            for (ProductOrderRequest itemReq : requests) {
                Product product = productRepository.findById(itemReq.productId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.productId()));

                ProductOrder po = new ProductOrder();
                po.setOrder(order);
                po.setProduct(product);
                po.setQuantity(itemReq.quantity());
                productOrders.add(po);
            }
        }
        return productOrders;
    }

    private double calculateTotalAmount(List<ProductOrder> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        return items.stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getUnitPrice() != null && item.getQuantity() != null)
                .mapToDouble(item -> item.getProduct().getUnitPrice() * item.getQuantity())
                .sum();
    }

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        OrderResponse response = modelMapper.map(order, OrderResponse.class);

        if (order.getCustomer() != null) {
            response.setCustomerId(order.getCustomer().getId());
            response.setCustomerName(order.getCustomer().getCustomerName());
        }

        if (order.getProductOrders() != null && !order.getProductOrders().isEmpty()) {
            List<ProductOrderResponse> poResponses = order.getProductOrders().stream()
                    .map(this::toProductOrderResponse)
                    .toList();
            response.setProductOrders(poResponses);
        } else {
            response.setProductOrders(new ArrayList<>());
        }

        return response;
    }

    private ProductOrderResponse toProductOrderResponse(ProductOrder po) {
        ProductOrderResponse poResp = modelMapper.map(po, ProductOrderResponse.class);

        if (po.getProduct() != null) {
            Product p = po.getProduct();
            poResp.setProductId(p.getId());
            poResp.setProductName(p.getProductName());
            poResp.setUnitPrice(p.getUnitPrice());
            if (p.getUnitPrice() != null && po.getQuantity() != null) {
                poResp.setSubtotal(p.getUnitPrice() * po.getQuantity());
            }
        }
        return poResp;
    }
}
