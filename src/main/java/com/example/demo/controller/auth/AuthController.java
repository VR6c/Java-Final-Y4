package com.example.demo.controller.auth;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.request.auth.LoginRequest;
import com.example.demo.dto.request.auth.RefreshTokenRequest;
import com.example.demo.dto.request.auth.RegisterRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.auth.AuthResponse;
import com.example.demo.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, JWT login, and token refresh")
@SecurityRequirements // Marks all /auth endpoints as public in Swagger
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response, AppConstants.SUCCESS_REGISTER));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Enter your email and password to receive JWT access and refresh tokens")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success(HttpStatus.OK.value(), response, AppConstants.SUCCESS_LOGIN);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Submit a valid refresh token to receive a new access token and refresh token")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ApiResponse.success(HttpStatus.OK.value(), response, AppConstants.SUCCESS_TOKEN_REFRESH);
    }
}
