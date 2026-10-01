package com.example.demo.controller.otp;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.request.otp.SendOtpRequest;
import com.example.demo.dto.request.otp.VerifyOtpRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.otp.OtpResponse;
import com.example.demo.service.otp.OtpService;
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
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
@Tag(name = "OTP Verification", description = "Endpoints for generating, delivering, and verifying Email OTP codes")
@SecurityRequirements // Public endpoint, no Bearer token required
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    @Operation(summary = "Send OTP code", description = "Generates a 6-digit OTP and delivers it to the target email address")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = otpService.sendOtp(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response, AppConstants.SUCCESS_OTP_SENT));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP code", description = "Validates the submitted OTP against the cached value for the target email")
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpResponse response = otpService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response, AppConstants.SUCCESS_OTP_VERIFIED));
    }

    @PostMapping("/resend")
    @Operation(summary = "Resend OTP code", description = "Invalidates the previous OTP and delivers a fresh OTP to the target email")
    public ResponseEntity<ApiResponse<OtpResponse>> resendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = otpService.resendOtp(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), response, AppConstants.SUCCESS_OTP_SENT));
    }
}
