package com.example.demo.service.school;

import com.example.demo.dto.patch.school.MajorPatchRequest;
import com.example.demo.dto.request.school.MajorRequest;
import com.example.demo.dto.response.school.MajorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MajorService {

    Page<MajorResponse> getAll(Pageable pageable);

    MajorResponse getById(Long id);

    MajorResponse create(MajorRequest request);

    MajorResponse update(Long id, MajorRequest input);

    MajorResponse edit(Long id, MajorPatchRequest input);

    void deleteById(Long id);

    Page<MajorResponse> search(String name, String description, Pageable pageable);
}

