package com.example.demo.service.auth;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.auth.PendingRegistration;
import com.example.demo.dto.request.auth.LoginRequest;
import com.example.demo.dto.request.auth.RefreshTokenRequest;
import com.example.demo.dto.request.auth.RegisterRequest;
import com.example.demo.dto.request.otp.VerifyOtpRequest;
import com.example.demo.dto.response.auth.AuthResponse;
import com.example.demo.dto.response.otp.OtpResponse;
import com.example.demo.entity.auth.Role;
import com.example.demo.entity.auth.User;
import com.example.demo.repository.auth.UserRepository;
import com.example.demo.service.mail.OtpMailService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int OTP_LENGTH = 6;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpMailService mailService;

    private final SecureRandom secureRandom = new SecureRandom();
    private Cache<String, PendingRegistration> pendingRegistrationCache;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.mail.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @PostConstruct
    public void init() {
        this.pendingRegistrationCache = Caffeine.newBuilder()
                .expireAfterWrite(expirationMinutes, TimeUnit.MINUTES)
                .maximumSize(5000)
                .recordStats()
                .build();
        log.info("Initialized Pending Registration Caffeine Cache with {} minutes TTL", expirationMinutes);
    }

    /**
     * Step 1: Initiate user registration.
     * Validates email uniqueness, caches pending registration for 5 minutes, and delivers OTP to user's email.
     */
    public OtpResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered: " + normalizedEmail);
        }

        Role userRole = request.getRole() != null ? request.getRole() : Role.ROLE_USER;
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String otp = generateOtpCode();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(expirationMinutes);

        PendingRegistration pending = new PendingRegistration(
                request.getFullName().trim(),
                normalizedEmail,
                encodedPassword,
                userRole,
                otp,
                now,
                expiresAt,
                new AtomicInteger(0)
        );

        pendingRegistrationCache.put(normalizedEmail, pending);
        log.info("Stored pending registration for [{}] with OTP, valid until {}", normalizedEmail, expiresAt);

        // Send OTP email
        mailService.sendOtpEmail(normalizedEmail, otp, expirationMinutes);

        return OtpResponse.builder()
                .email(normalizedEmail)
                .verified(false)
                .message(AppConstants.SUCCESS_REGISTRATION_OTP_SENT)
                .expiresInMinutes(expirationMinutes)
                .timestamp(now)
                .build();
    }

    /**
     * Step 2: Verify OTP code and commit user registration.
     * Persists user to database, invalidates cache, and issues JWT tokens.
     */
    @Transactional
    public AuthResponse verifyRegister(VerifyOtpRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        PendingRegistration pending = pendingRegistrationCache.getIfPresent(normalizedEmail);

        if (pending == null) {
            log.warn("Registration OTP verification failed for [{}]: No active session or expired", normalizedEmail);
            throw new IllegalArgumentException("Verification code has expired or registration session not found. Please register again.");
        }

        if (LocalDateTime.now().isAfter(pending.expiresAt())) {
            pendingRegistrationCache.invalidate(normalizedEmail);
            log.warn("Registration OTP verification failed for [{}]: Code expired", normalizedEmail);
            throw new IllegalArgumentException("Verification code has expired. Please register again.");
        }

        int attempts = pending.attempts().incrementAndGet();
        if (attempts > MAX_VERIFICATION_ATTEMPTS) {
            pendingRegistrationCache.invalidate(normalizedEmail);
            log.warn("Registration OTP verification failed for [{}]: Exceeded {} attempts", normalizedEmail, MAX_VERIFICATION_ATTEMPTS);
            throw new IllegalArgumentException("Maximum verification attempts exceeded. Please register again.");
        }

        if (!pending.otp().equals(request.getOtp().trim())) {
            int remaining = MAX_VERIFICATION_ATTEMPTS - attempts;
            log.warn("Registration OTP mismatch for [{}]. Remaining attempts: {}", normalizedEmail, remaining);
            throw new IllegalArgumentException(String.format("Invalid OTP code. %d attempt(s) remaining.", remaining));
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            pendingRegistrationCache.invalidate(normalizedEmail);
            throw new IllegalArgumentException("Email is already registered: " + normalizedEmail);
        }

        // Successfully verified -> persist user
        User user = User.builder()
                .fullName(pending.fullName())
                .email(pending.email())
                .password(pending.encodedPassword())
                .role(pending.role())
                .build();

        userRepository.save(user);

        // Invalidate cache immediately to prevent replay
        pendingRegistrationCache.invalidate(normalizedEmail);
        log.info("User registered and verified successfully: [{}]", normalizedEmail);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    /**
     * Resend a fresh OTP for a pending registration session (resets 5-minute countdown).
     */
    public OtpResponse resendRegistrationOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        PendingRegistration pending = pendingRegistrationCache.getIfPresent(normalizedEmail);

        if (pending == null) {
            log.warn("Resend registration OTP failed for [{}]: No active pending registration found", normalizedEmail);
            throw new IllegalArgumentException("No pending registration session found for this email. Please register again.");
        }

        String newOtp = generateOtpCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(expirationMinutes);

        PendingRegistration updated = new PendingRegistration(
                pending.fullName(),
                pending.email(),
                pending.encodedPassword(),
                pending.role(),
                newOtp,
                now,
                expiresAt,
                new AtomicInteger(0)
        );

        pendingRegistrationCache.put(normalizedEmail, updated);
        log.info("Refreshed registration OTP for [{}], valid until {}", normalizedEmail, expiresAt);

        mailService.sendOtpEmail(normalizedEmail, newOtp, expirationMinutes);

        return OtpResponse.builder()
                .email(normalizedEmail)
                .verified(false)
                .message(AppConstants.SUCCESS_OTP_SENT)
                .expiresInMinutes(expirationMinutes)
                .timestamp(now)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid or malformed refresh token");
        }

        if (userEmail == null) {
            throw new BadCredentialsException("Refresh token does not contain a subject");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadCredentialsException("Refresh token is expired or invalid");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
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
}
