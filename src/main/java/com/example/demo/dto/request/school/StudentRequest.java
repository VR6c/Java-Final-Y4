package com.example.demo.dto.request.school;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StudentRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be at least 0")
    Integer age,

    @Min(value = 1, message = "Major ID must be greater than or equal to 1")
    Long majorId,

    List<@NotNull(message = "Subject ID cannot be null") @Min(value = 1, message = "Subject ID must be greater than or equal to 1") Long> subjectIds
) {}
