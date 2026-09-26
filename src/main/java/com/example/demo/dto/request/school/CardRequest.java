package com.example.demo.dto.request.school;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CardRequest(
    String cardNumber,

    @NotNull(message = "Issue date is required")
    LocalDate issueDate,

    @Min(value = 1, message = "Student ID must be greater than or equal to 1")
    Long studentId
) {}
