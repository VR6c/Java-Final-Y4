package com.example.demo.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotBlank(message = "Product name is required")
    String productName,

    @Size(max = 100, message = "Description must not exceed 100 characters")
    String description,

    @NotNull(message = "Unit price is required")
    @Min(value = 0, message = "Unit price must be non-negative")
    Double unitPrice
) {}
