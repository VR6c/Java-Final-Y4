package com.example.demo.service.product.serviceImpl;

import com.example.demo.dto.patch.product.ProductOrderPatchRequest;
import com.example.demo.dto.response.product.ProductOrderResponse;
import com.example.demo.entity.product.Order;
import com.example.demo.entity.product.Product;
import com.example.demo.entity.product.ProductOrder;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.product.OrderRepository;
import com.example.demo.repository.product.ProductOrderRepository;
import com.example.demo.service.product.ProductOrderService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductOrderServiceImpl implements ProductOrderService {

    private final ProductOrderRepository productOrderRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    public ProductOrderServiceImpl(ProductOrderRepository productOrderRepository, OrderRepository orderRepository, ModelMapper modelMapper) {
        this.productOrderRepository = productOrderRepository;
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<ProductOrderResponse> getAll(Pageable pageable) {
        return productOrderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public ProductOrderResponse getById(Long id) {
        return toResponse(findProductOrderById(id));
    }

    @Override
    public ProductOrderResponse edit(Long id, ProductOrderPatchRequest request) {
        ProductOrder existingItem = findProductOrderById(id);

        if (request.quantity() != null) {
            existingItem.setQuantity(request.quantity());
            ProductOrder savedItem = productOrderRepository.save(existingItem);
            recalculateOrderTotal(savedItem.getOrder());
            return toResponse(savedItem);
        }

        return toResponse(existingItem);
    }

    @Override
    public void deleteById(Long id) {
        ProductOrder item = findProductOrderById(id);
        Order order = item.getOrder();
        if (order != null && order.getProductOrders() != null) {
            order.getProductOrders().remove(item);
        }
        productOrderRepository.delete(item);
        if (order != null) {
            recalculateOrderTotal(order);
        }
    }

    private ProductOrder findProductOrderById(Long id) {
        return productOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductOrder not found with id: " + id));
    }

    private void recalculateOrderTotal(Order order) {
        if (order == null || order.getProductOrders() == null) {
            return;
        }
        double total = order.getProductOrders().stream()
                .filter(po -> po.getProduct() != null && po.getProduct().getUnitPrice() != null && po.getQuantity() != null)
                .mapToDouble(po -> po.getProduct().getUnitPrice() * po.getQuantity())
                .sum();
        order.setTotalAmount(total);
        orderRepository.save(order);
    }

    public ProductOrderResponse toResponse(ProductOrder po) {
        if (po == null) {
            return null;
        }
        ProductOrderResponse response = modelMapper.map(po, ProductOrderResponse.class);
        if (po.getProduct() != null) {
            Product product = po.getProduct();
            response.setProductId(product.getId());
            response.setProductName(product.getProductName());
            response.setUnitPrice(product.getUnitPrice());
            if (product.getUnitPrice() != null && po.getQuantity() != null) {
                response.setSubtotal(product.getUnitPrice() * po.getQuantity());
            }
        }
        return response;
    }
}
