package com.example.demo.controller.logging;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.dto.response.logging.ActivityLogResponse;
import com.example.demo.service.logging.ActivityLogService;
import com.example.demo.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
@Validated
@Hidden
@Tag(name = "Activity Logs", description = "Endpoints for viewing user and system activity audit logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all activity logs", description = "Returns a paginated list of all system and user activities")
    public ApiResponse<PaginationResponse<ActivityLogResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<ActivityLogResponse> resultPage = activityLogService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(resultPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Search activity logs", description = "Search activities filtered by username, action, or resource")
    public ApiResponse<PaginationResponse<ActivityLogResponse>> search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<ActivityLogResponse> searchResult = activityLogService.search(username, action, resource, pageable);
        return ApiResponse.success(PaginationResponse.from(searchResult), AppConstants.SUCCESS_FETCH_ALL);
    }
}
