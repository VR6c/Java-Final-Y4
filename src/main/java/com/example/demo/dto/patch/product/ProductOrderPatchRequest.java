package com.example.demo.dto.patch.product;

import jakarta.validation.constraints.Min;

public record ProductOrderPatchRequest(
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {}
