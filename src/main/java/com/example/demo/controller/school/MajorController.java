package com.example.demo.controller.school;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.school.MajorPatchRequest;
import com.example.demo.dto.request.school.MajorRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.school.MajorResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.service.school.MajorService;
import com.example.demo.util.PaginationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;

@Hidden
@RestController
@RequestMapping("/majors")
@Validated
public class MajorController {

    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    public ApiResponse<PaginationResponse<MajorResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<MajorResponse> majorPage = majorService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(majorPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MajorResponse>> create(@Valid @RequestBody MajorRequest major) {
        MajorResponse savedMajor = majorService.create(major);
        return ResponseEntity.created(URI.create("/majors/" + savedMajor.getId()))
                .body(ApiResponse.success(HttpStatus.CREATED.value(), savedMajor, AppConstants.SUCCESS_CREATE));
    }

    @GetMapping("/{id}")
    public ApiResponse<MajorResponse> getById(@PathVariable @Min(1) Long id) {
        MajorResponse major = majorService.getById(id);
        return ApiResponse.success(major, AppConstants.SUCCESS_RETRIEVE);
    }

    @PutMapping("/{id}")
    public ApiResponse<MajorResponse> update(@PathVariable @Min(1) Long id, @Valid @RequestBody MajorRequest input) {
        MajorResponse updated = majorService.update(id, input);
        return ApiResponse.success(updated, AppConstants.SUCCESS_UPDATE);
    }

    @PatchMapping("/{id}")
    public ApiResponse<MajorResponse> edit(@PathVariable @Min(1) Long id, @Valid @RequestBody MajorPatchRequest input) {
        MajorResponse edited = majorService.edit(id, input);
        return ApiResponse.success(edited, AppConstants.SUCCESS_UPDATE);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) Long id) {
        majorService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ApiResponse<PaginationResponse<MajorResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<MajorResponse> searchResult = majorService.search(name, description, pageable);
        return ApiResponse.success(PaginationResponse.from(searchResult), AppConstants.SUCCESS_FETCH_ALL);
    }
}
