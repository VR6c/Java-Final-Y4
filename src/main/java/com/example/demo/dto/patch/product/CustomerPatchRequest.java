package com.example.demo.dto.patch.product;

import jakarta.validation.constraints.Size;

public record CustomerPatchRequest(
    @Size(max = 30, message = "Customer name must not exceed 30 characters")
    String customerName,

    @Size(max = 20, message = "Address must not exceed 20 characters")
    String address,

    @Size(max = 12, message = "Phone number must not exceed 12 characters")
    String phoneNumber,

    Long emailId,

    Boolean unlinkEmail
) {}
