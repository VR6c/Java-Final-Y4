package com.example.demo.service.product;

import com.example.demo.dto.patch.product.EmailPatchRequest;
import com.example.demo.dto.request.product.EmailRequest;
import com.example.demo.dto.response.product.EmailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmailService {

    Page<EmailResponse> getAll(Pageable pageable);

    EmailResponse getById(Long id);

    EmailResponse create(EmailRequest request);

    EmailResponse update(Long id, EmailRequest request);

    EmailResponse edit(Long id, EmailPatchRequest request);

    void deleteById(Long id);

    Page<EmailResponse> search(String email, Pageable pageable);
}
