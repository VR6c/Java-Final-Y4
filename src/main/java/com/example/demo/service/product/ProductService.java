package com.example.demo.service.product;

import com.example.demo.dto.patch.product.ProductPatchRequest;
import com.example.demo.dto.request.product.ProductRequest;
import com.example.demo.dto.response.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductResponse> getAll(Pageable pageable);

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse edit(Long id, ProductPatchRequest request);

    void deleteById(Long id);

    Page<ProductResponse> search(String name, Double minPrice, Double maxPrice, Pageable pageable);
}
