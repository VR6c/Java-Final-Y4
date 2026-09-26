package com.example.demo.service.product.serviceImpl;

import com.example.demo.dto.patch.product.EmailPatchRequest;
import com.example.demo.dto.request.product.EmailRequest;
import com.example.demo.dto.response.product.EmailResponse;
import com.example.demo.entity.product.Email;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.product.EmailRepository;
import com.example.demo.service.product.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmailServiceImpl implements EmailService {

    private final EmailRepository emailRepository;
    private final ModelMapper modelMapper;

    public EmailServiceImpl(EmailRepository emailRepository, ModelMapper modelMapper) {
        this.emailRepository = emailRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<EmailResponse> getAll(Pageable pageable) {
        return emailRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public EmailResponse getById(Long id) {
        return toResponse(findEmailById(id));
    }

    @Override
    public EmailResponse create(EmailRequest request) {
        if (emailRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        Email email = new Email();
        email.setEmail(request.email());

        return toResponse(emailRepository.save(email));
    }

    @Override
    public EmailResponse update(Long id, EmailRequest request) {
        Email existingEmail = findEmailById(id);

        if (emailRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        existingEmail.setEmail(request.email());
        return toResponse(emailRepository.save(existingEmail));
    }

    @Override
    public EmailResponse edit(Long id, EmailPatchRequest request) {
        Email existingEmail = findEmailById(id);

        if (request.email() != null && !request.email().trim().isEmpty()) {
            String newEmail = request.email().trim();
            if (emailRepository.existsByEmailAndIdNot(newEmail, id)) {
                throw new IllegalArgumentException("Email already exists: " + newEmail);
            }
            existingEmail.setEmail(newEmail);
        }

        return toResponse(emailRepository.save(existingEmail));
    }

    @Override
    public void deleteById(Long id) {
        emailRepository.delete(findEmailById(id));
    }

    @Override
    public Page<EmailResponse> search(String email, Pageable pageable) {
        return emailRepository.searchEmails(email, pageable)
                .map(this::toResponse);
    }

    private Email findEmailById(Long id) {
        return emailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found with id: " + id));
    }

    public EmailResponse toResponse(Email email) {
        if (email == null) {
            return null;
        }
        EmailResponse response = modelMapper.map(email, EmailResponse.class);
        if (email.getCustomer() != null) {
            response.setCustomerId(email.getCustomer().getId());
            response.setCustomerName(email.getCustomer().getCustomerName());
        }
        return response;
    }
}
