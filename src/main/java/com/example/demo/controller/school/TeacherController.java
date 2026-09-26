package com.example.demo.controller.school;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.school.TeacherPatchRequest;
import com.example.demo.dto.request.school.TeacherRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.dto.response.school.TeacherResponse;
import com.example.demo.service.school.TeacherService;
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
@RequestMapping("/teachers")
@Validated
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ApiResponse<PaginationResponse<TeacherResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {
        
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<TeacherResponse> teacherPage = teacherService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(teacherPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeacherResponse>> create(@Valid @RequestBody TeacherRequest teacher) {
        TeacherResponse savedTeacher = teacherService.create(teacher);
        return ResponseEntity.created(URI.create("/teachers/" + savedTeacher.getId()))
                .body(ApiResponse.success(HttpStatus.CREATED.value(), savedTeacher, AppConstants.SUCCESS_CREATE));
    }

    @GetMapping("/{id}")
    public ApiResponse<TeacherResponse> getById(@PathVariable @Min(1) Long id) {
        TeacherResponse teacher = teacherService.getById(id);
        return ApiResponse.success(teacher, AppConstants.SUCCESS_RETRIEVE);
    }

    @PutMapping("/{id}")
    public ApiResponse<TeacherResponse> update(@PathVariable @Min(1) Long id, @Valid @RequestBody TeacherRequest input) {
        TeacherResponse updated = teacherService.update(id, input);
        return ApiResponse.success(updated, AppConstants.SUCCESS_UPDATE);
    }

    @PatchMapping("/{id}")
    public ApiResponse<TeacherResponse> edit(@PathVariable @Min(1) Long id, @Valid @RequestBody TeacherPatchRequest input) {
        TeacherResponse edited = teacherService.edit(id, input);
        return ApiResponse.success(edited, AppConstants.SUCCESS_UPDATE);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) Long id) {
        teacherService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ApiResponse<PaginationResponse<TeacherResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<TeacherResponse> searchResult = teacherService.search(name, subject, pageable);
        return ApiResponse.success(PaginationResponse.from(searchResult), AppConstants.SUCCESS_FETCH_ALL);
    }
}