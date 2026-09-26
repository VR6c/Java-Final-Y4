package com.example.demo.dto.request.school;

import jakarta.validation.constraints.NotBlank;

public record TeacherRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Subject is required")
    String subject
) {}
