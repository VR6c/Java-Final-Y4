package com.example.demo.service.product;

import com.example.demo.dto.patch.product.CustomerPatchRequest;
import com.example.demo.dto.request.product.CustomerRequest;
import com.example.demo.dto.response.product.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    Page<CustomerResponse> getAll(Pageable pageable);

    CustomerResponse getById(Long id);

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest request);

    CustomerResponse edit(Long id, CustomerPatchRequest request);

    void deleteById(Long id);

    Page<CustomerResponse> search(String name, String phone, Pageable pageable);
}
