package com.example.demo.service.school;

import com.example.demo.dto.patch.school.SubjectPatchRequest;
import com.example.demo.dto.request.school.SubjectRequest;
import com.example.demo.dto.response.school.SubjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubjectService {

    Page<SubjectResponse> getAll(Pageable pageable);

    SubjectResponse getById(Long id);

    SubjectResponse create(SubjectRequest request);

    SubjectResponse update(Long id, SubjectRequest input);

    SubjectResponse edit(Long id, SubjectPatchRequest input);

    void deleteById(Long id);

    Page<SubjectResponse> search(String name, String description, Pageable pageable);
}
