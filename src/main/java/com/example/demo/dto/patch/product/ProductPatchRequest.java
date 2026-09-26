package com.example.demo.dto.patch.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProductPatchRequest(
    String productName,

    @Size(max = 100, message = "Description must not exceed 100 characters")
    String description,

    @Min(value = 0, message = "Unit price must be non-negative")
    Double unitPrice
) {}
