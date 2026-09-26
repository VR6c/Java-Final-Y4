package com.example.demo.service.school;

import com.example.demo.dto.patch.school.StudentPatchRequest;
import com.example.demo.dto.request.school.StudentRequest;
import com.example.demo.dto.response.school.StudentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    Page<StudentResponse> getAll(Pageable pageable);

    StudentResponse create(StudentRequest request);

    StudentResponse getById(Long id);

    StudentResponse update(Long id, StudentRequest input);

    StudentResponse edit(Long id, StudentPatchRequest input);

    void deleteById(Long id);

    Page<StudentResponse> search(String name, Integer minAge, Integer maxAge, Pageable pageable);
}
