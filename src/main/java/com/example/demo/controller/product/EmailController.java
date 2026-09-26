package com.example.demo.controller.product;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.product.EmailPatchRequest;
import com.example.demo.dto.request.product.EmailRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.product.EmailResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.service.product.EmailService;
import com.example.demo.util.PaginationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/emails")
@Validated
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public ApiResponse<PaginationResponse<EmailResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<EmailResponse> emailPage = emailService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(emailPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmailResponse>> create(@Valid @RequestBody EmailRequest request) {
        EmailResponse savedEmail = emailService.create(request);
        return ResponseEntity.created(URI.create("/emails/" + savedEmail.getId()))
                .body(ApiResponse.success(HttpStatus.CREATED.value(), savedEmail, AppConstants.SUCCESS_CREATE));
    }

    @GetMapping("/{id}")
    public ApiResponse<EmailResponse> getById(@PathVariable @Min(1) Long id) {
        EmailResponse email = emailService.getById(id);
        return ApiResponse.success(email, AppConstants.SUCCESS_RETRIEVE);
    }

    @PutMapping("/{id}")
    public ApiResponse<EmailResponse> update(@PathVariable @Min(1) Long id, @Valid @RequestBody EmailRequest input) {
        EmailResponse updated = emailService.update(id, input);
        return ApiResponse.success(updated, AppConstants.SUCCESS_UPDATE);
    }

    @PatchMapping("/{id}")
    public ApiResponse<EmailResponse> edit(@PathVariable @Min(1) Long id, @Valid @RequestBody EmailPatchRequest input) {
        EmailResponse edited = emailService.edit(id, input);
        return ApiResponse.success(edited, AppConstants.SUCCESS_UPDATE);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) Long id) {
        emailService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ApiResponse<PaginationResponse<EmailResponse>> search(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<EmailResponse> searchResult = emailService.search(email, pageable);
        return ApiResponse.success(PaginationResponse.from(searchResult), AppConstants.SUCCESS_FETCH_ALL);
    }
}
