package com.example.demo.service.otp;

import com.example.demo.dto.response.otp.OtpResponse;
import com.example.demo.service.mail.OtpMailService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    private final OtpMailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.otp.expiration-minutes:5}")
    private int expirationMinutes;

    private Cache<String, OtpRecord> otpCache;

    public OtpServiceImpl(OtpMailService mailService) {
        this.mailService = mailService;
    }

    @PostConstruct
    public void init() {
        this.otpCache = Caffeine.newBuilder()
                .expireAfterWrite(expirationMinutes, TimeUnit.MINUTES)
                .maximumSize(10000)
                .recordStats()
                .build();
        log.info("Initialized OTP Caffeine Cache with {} minutes TTL", expirationMinutes);
    }

    @Override
    public OtpResponse sendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        String otp = generateOtpCode();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(expirationMinutes);

        // Store OTP in cache
        OtpRecord record = new OtpRecord(otp, now, expiresAt, new AtomicInteger(0));
        otpCache.put(normalizedEmail, record);
        log.info("Generated new OTP for [{}], valid until {}", normalizedEmail, expiresAt);

        // Send OTP email
        mailService.sendOtpEmail(normalizedEmail, otp, expirationMinutes);

        return OtpResponse.builder()
                .email(normalizedEmail)
                .message(com.example.demo.constant.AppConstants.SUCCESS_OTP_SENT)
                .expiresInMinutes(expirationMinutes)
                .timestamp(now)
                .build();
    }

    @Override
    public OtpResponse verifyOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        OtpRecord record = otpCache.getIfPresent(normalizedEmail);

        if (record == null) {
            log.warn("OTP verification failed for [{}]: No active OTP found or expired", normalizedEmail);
            throw new IllegalArgumentException("OTP code has expired or does not exist. Please request a new code.");
        }

        if (LocalDateTime.now().isAfter(record.expiresAt())) {
            otpCache.invalidate(normalizedEmail);
            log.warn("OTP verification failed for [{}]: Code expired", normalizedEmail);
            throw new IllegalArgumentException("OTP code has expired. Please request a new code.");
        }

        int currentAttempts = record.attempts().incrementAndGet();
        if (currentAttempts > MAX_VERIFICATION_ATTEMPTS) {
            otpCache.invalidate(normalizedEmail);
            log.warn("OTP verification failed for [{}]: Exceeded {} attempts", normalizedEmail, MAX_VERIFICATION_ATTEMPTS);
            throw new IllegalArgumentException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        if (!record.otp().equals(otp.trim())) {
            int remainingAttempts = MAX_VERIFICATION_ATTEMPTS - currentAttempts;
            log.warn("OTP mismatch for [{}]. Remaining attempts: {}", normalizedEmail, remainingAttempts);
            throw new IllegalArgumentException(String.format("Invalid OTP code. %d attempt(s) remaining.", remainingAttempts));
        }

        // Successfully verified -> invalidate immediately to prevent replay attacks
        otpCache.invalidate(normalizedEmail);
        log.info("OTP verified successfully for [{}]", normalizedEmail);

        return OtpResponse.builder()
                .email(normalizedEmail)
                .verified(true)
                .message(com.example.demo.constant.AppConstants.SUCCESS_RETRIEVE)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public OtpResponse resendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        otpCache.invalidate(normalizedEmail);
        log.info("Invalidated previous OTP and resending new OTP to [{}]", normalizedEmail);
        return sendOtp(normalizedEmail);
    }

    private String generateOtpCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int min = (int) Math.pow(10, OTP_LENGTH - 1);
        int code = min + secureRandom.nextInt(bound - min);
        return String.valueOf(code);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        return email.trim().toLowerCase();
    }

    private record OtpRecord(
            String otp,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            AtomicInteger attempts
    ) {}
}
