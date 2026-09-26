package com.example.demo.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(
    String orderDate,

    String status,

    @NotNull(message = "Customer ID is required")
    @Min(value = 1, message = "Customer ID must be greater than or equal to 1")
    Long customerId,

    @Valid
    List<ProductOrderRequest> productOrders
) {}
