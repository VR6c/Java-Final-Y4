package com.example.demo.dto.response.school;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SubjectResponse {
    private Long id;
    private String name;
    private String description;
    private List<StudentResponse> students;
}
