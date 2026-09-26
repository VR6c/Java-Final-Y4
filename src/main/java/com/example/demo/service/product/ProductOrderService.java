package com.example.demo.service.product;

import com.example.demo.dto.patch.product.ProductOrderPatchRequest;
import com.example.demo.dto.response.product.ProductOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductOrderService {

    Page<ProductOrderResponse> getAll(Pageable pageable);

    ProductOrderResponse getById(Long id);

    ProductOrderResponse edit(Long id, ProductOrderPatchRequest request);

    void deleteById(Long id);
}
