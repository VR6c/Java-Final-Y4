package com.example.demo.service.otp;

import com.example.demo.dto.response.otp.OtpResponse;

public interface OtpService {

    /**
     * Generate a 6-digit OTP, store it with TTL, and dispatch an email.
     *
     * @param email Target recipient email
     * @return OtpResponse with operation details
     */
    OtpResponse sendOtp(String email);

    /**
     * Verify the provided OTP code against the stored value for this email.
     *
     * @param email Target email
     * @param otp   User-submitted 6-digit OTP
     * @return OtpResponse indicating verification result
     */
    OtpResponse verifyOtp(String email, String otp);

    /**
     * Invalidate any previous OTP and issue a brand new OTP.
     *
     * @param email Target recipient email
     * @return OtpResponse with operation details
     */
    OtpResponse resendOtp(String email);
}
