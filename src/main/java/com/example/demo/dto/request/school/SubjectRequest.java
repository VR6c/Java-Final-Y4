package com.example.demo.dto.request.school;

import jakarta.validation.constraints.NotBlank;

public record SubjectRequest(
    @NotBlank(message = "Name is required")
    String name,
    String description
) {}
