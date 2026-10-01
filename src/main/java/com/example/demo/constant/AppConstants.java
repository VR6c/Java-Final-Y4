package com.example.demo.constant;

public final class AppConstants {
    private AppConstants() {
    }

    // Pagination Constants
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    // Response Messages
    public static final String SUCCESS_CREATE = "Resource created successfully";
    public static final String SUCCESS_UPDATE = "Resource updated successfully";
    public static final String SUCCESS_RETRIEVE = "Resource retrieved successfully";
    public static final String SUCCESS_DELETE = "Resource deleted successfully";
    public static final String SUCCESS_FETCH_ALL = "Resources fetched successfully";

    // Authentication & OTP Response Messages
    public static final String SUCCESS_REGISTER = "User registered successfully";
    public static final String SUCCESS_LOGIN = "Authentication successful";
    public static final String SUCCESS_TOKEN_REFRESH = "Token refreshed successfully";
    public static final String SUCCESS_LOGOUT = "User logged out successfully";
    public static final String SUCCESS_OTP_SENT = "OTP sent successfully";
    public static final String SUCCESS_REGISTRATION_OTP_SENT = "Registration initiated. Verification OTP has been sent to your email";
    public static final String SUCCESS_OTP_VERIFIED = "OTP verified successfully";

    // Error Messages
    public static final String ERROR_NOT_FOUND = "Resource not found";
    public static final String ERROR_BAD_REQUEST = "Invalid input or bad request";
    public static final String ERROR_INTERNAL_SERVER = "An unexpected error occurred";
    public static final String ERROR_VALIDATION = "Validation failure";

    // Card Formatting Helpers
    public static String formatCardNumber(String majorName, Long studentId) {
        if (majorName != null && !majorName.trim().isEmpty()) {
            return String.format("%s - %04d", majorName.trim(), studentId);
        }
        return formatDefaultCardNumber(studentId);
    }

    public static String formatDefaultCardNumber(Long studentId) {
        return String.format("STUDENT - %04d", studentId);
    }
}
