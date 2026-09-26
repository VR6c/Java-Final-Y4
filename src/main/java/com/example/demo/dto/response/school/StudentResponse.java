package com.example.demo.dto.response.school;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class StudentResponse {
    private Long id;
    private String name;
    private Integer age;
    private Long cardId;
    private String cardNumber;
    private LocalDate issueDate;
    private Long majorId;
    private String majorName;
    private List<Long> subjectIds;
    private List<String> subjectNames;
}
