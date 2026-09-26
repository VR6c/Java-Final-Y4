package com.example.demo.controller.auth;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.request.auth.UpdateUserRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.auth.UserResponse;
import com.example.demo.entity.auth.User;
import com.example.demo.service.auth.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "app-user-controller", description = "Endpoints for managing the currently authenticated user profile and session")
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping("/current")
    @Operation(summary = "Get current authenticated user profile", description = "Retrieves profile details of the user identified by the Bearer token")
    public ApiResponse<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        UserResponse response = appUserService.getCurrentUser(currentUser);
        return ApiResponse.success(HttpStatus.OK.value(), response, AppConstants.SUCCESS_RETRIEVE);
    }

    @PutMapping
    @Operation(summary = "Update current user profile", description = "Updates details (such as full name or password) for the currently authenticated user")
    public ApiResponse<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = appUserService.updateCurrentUser(currentUser, request);
        return ApiResponse.success(HttpStatus.OK.value(), response, AppConstants.SUCCESS_UPDATE);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Clears the server-side security context for the current user session")
    public ApiResponse<Void> logout() {
        appUserService.logout();
        return ApiResponse.success(HttpStatus.OK.value(), null, AppConstants.SUCCESS_LOGOUT);
    }
}
