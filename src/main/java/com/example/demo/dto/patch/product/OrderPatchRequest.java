package com.example.demo.dto.patch.product;

import com.example.demo.dto.request.product.ProductOrderRequest;
import jakarta.validation.Valid;

import java.util.List;

public record OrderPatchRequest(
    String orderDate,

    String status,

    Long customerId,

    Boolean unlinkCustomer,

    @Valid
    List<ProductOrderRequest> productOrders,

    Boolean clearProductOrders
) {}
