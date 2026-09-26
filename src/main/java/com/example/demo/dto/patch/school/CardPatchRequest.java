package com.example.demo.dto.patch.school;

import java.time.LocalDate;

public record CardPatchRequest(
    String cardNumber,
    LocalDate issueDate,
    Long studentId,
    Boolean unlinkStudent
) {}
