package com.example.demo.service.product;

import com.example.demo.dto.patch.product.OrderPatchRequest;
import com.example.demo.dto.request.product.OrderRequest;
import com.example.demo.dto.response.product.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<OrderResponse> getAll(Pageable pageable);

    OrderResponse getById(Long id);

    OrderResponse create(OrderRequest request);

    OrderResponse update(Long id, OrderRequest request);

    OrderResponse edit(Long id, OrderPatchRequest request);

    void deleteById(Long id);

    Page<OrderResponse> search(String status, Long customerId, Pageable pageable);
}
