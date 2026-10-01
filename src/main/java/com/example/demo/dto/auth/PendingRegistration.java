package com.example.demo.dto.auth;

import com.example.demo.entity.auth.Role;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds pending user registration details in Caffeine cache until OTP verification is completed.
 */
public record PendingRegistration(
        String fullName,
        String email,
        String encodedPassword,
        Role role,
        String otp,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        AtomicInteger attempts
) {}
