package com.example.demo.controller.school;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.school.StudentPatchRequest;
import com.example.demo.dto.request.school.StudentRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.dto.response.school.StudentResponse;
import com.example.demo.service.school.StudentService;
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
@RequestMapping("/students")
@Validated
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResponse<PaginationResponse<StudentResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {
        
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<StudentResponse> studentPage = studentService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(studentPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> create(@Valid @RequestBody StudentRequest student) {
        StudentResponse savedStudent = studentService.create(student);
        return ResponseEntity.created(URI.create("/students/" + savedStudent.getId()))
                .body(ApiResponse.success(HttpStatus.CREATED.value(), savedStudent, AppConstants.SUCCESS_CREATE));
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentResponse> getById(@PathVariable @Min(1) Long id) {
        StudentResponse student = studentService.getById(id);
        return ApiResponse.success(student, AppConstants.SUCCESS_RETRIEVE);
    }

    @PutMapping("/{id}")
    public ApiResponse<StudentResponse> update(@PathVariable @Min(1) Long id, @Valid @RequestBody StudentRequest input) {
        StudentResponse updated = studentService.update(id, input);
        return ApiResponse.success(updated, AppConstants.SUCCESS_UPDATE);
    }

    @PatchMapping("/{id}")
    public ApiResponse<StudentResponse> edit(@PathVariable @Min(1) Long id, @Valid @RequestBody StudentPatchRequest input) {
        StudentResponse edited = studentService.edit(id, input);
        return ApiResponse.success(edited, AppConstants.SUCCESS_UPDATE);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) Long id) {
        studentService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ApiResponse<PaginationResponse<StudentResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @Min(0) Integer minAge,
            @RequestParam(required = false) @Min(0) Integer maxAge,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<StudentResponse> searchResult = studentService.search(name, minAge, maxAge, pageable);
        return ApiResponse.success(PaginationResponse.from(searchResult), AppConstants.SUCCESS_FETCH_ALL);
    }
}