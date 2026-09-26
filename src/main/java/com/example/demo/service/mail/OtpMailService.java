package com.example.demo.service.mail;

public interface OtpMailService {

    /**
     * Send OTP verification email using the branded HTML template.
     *
     * @param toEmail           Recipient email address
     * @param otp               Generated OTP code
     * @param expirationMinutes Expiration duration in minutes
     */
    void sendOtpEmail(String toEmail, String otp, int expirationMinutes);

    /**
     * Send general HTML email.
     *
     * @param toEmail     Recipient email address
     * @param subject     Email subject
     * @param htmlContent HTML body content
     */
    void sendHtmlEmail(String toEmail, String subject, String htmlContent);
}
