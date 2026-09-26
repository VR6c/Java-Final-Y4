package com.example.demo.util;

public class PhoneNumberUtils {

    private PhoneNumberUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * @param phone Raw phone number string
     * @return Formatted phone number string
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() == 10) {
            return digits.replaceAll("(\\d{3})(\\d{3})(\\d{4})", "$1 $2 $3");
        } else if (digits.length() == 9) {
            return digits.replaceAll("(\\d{3})(\\d{3})(\\d{3})", "$1 $2 $3");
        }

        return phone.trim();
    }
}
