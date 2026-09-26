package com.example.demo.dto.patch.product;

import jakarta.validation.constraints.Email;

public record EmailPatchRequest(
    @Email(message = "Invalid email format")
    String email
) {}
