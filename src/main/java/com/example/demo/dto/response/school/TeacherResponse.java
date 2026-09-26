package com.example.demo.dto.response.school;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeacherResponse {
    private Long id;
    private String name;
    private String subject;
}
