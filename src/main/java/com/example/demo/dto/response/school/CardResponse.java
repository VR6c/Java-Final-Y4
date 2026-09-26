package com.example.demo.dto.response.school;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CardResponse {
    private Long id;
    private String cardNumber;
    private LocalDate issueDate;
    private Long studentId;
    private String studentName;
}
