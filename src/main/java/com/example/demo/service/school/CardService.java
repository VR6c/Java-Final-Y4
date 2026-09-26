package com.example.demo.service.school;

import com.example.demo.dto.patch.school.CardPatchRequest;
import com.example.demo.dto.request.school.CardRequest;
import com.example.demo.dto.response.school.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

    Page<CardResponse> getAll(Pageable pageable);

    CardResponse getById(Long id);

    CardResponse create(CardRequest request);

    CardResponse update(Long id, CardRequest request);

    CardResponse edit(Long id, CardPatchRequest request);

    void deleteById(Long id);

    Page<CardResponse> search(String cardNumber, String studentName, Pageable pageable);
}
