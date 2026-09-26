package com.example.demo.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
    @NotBlank(message = "Customer name is required")
    @Size(max = 30, message = "Customer name must not exceed 30 characters")
    String customerName,

    @Size(max = 20, message = "Address must not exceed 20 characters")
    String address,

    @Size(max = 12, message = "Phone number must not exceed 12 characters")
    String phoneNumber,

    @Min(value = 1, message = "Email ID must be greater than or equal to 1")
    Long emailId
) {}
