package com.example.demo.service.school;

import com.example.demo.dto.patch.school.TeacherPatchRequest;
import com.example.demo.dto.request.school.TeacherRequest;
import com.example.demo.dto.response.school.TeacherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeacherService {

    Page<TeacherResponse> getAll(Pageable pageable);

    TeacherResponse create(TeacherRequest request);

    TeacherResponse getById(Long id);

    TeacherResponse update(Long id, TeacherRequest input);

    TeacherResponse edit(Long id, TeacherPatchRequest input);

    void deleteById(Long id);

    Page<TeacherResponse> search(String name, String subject, Pageable pageable);
}
