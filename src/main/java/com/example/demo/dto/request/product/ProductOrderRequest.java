package com.example.demo.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductOrderRequest(
    @NotNull(message = "Product ID is required")
    @Min(value = 1, message = "Product ID must be greater than or equal to 1")
    Long productId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {}
