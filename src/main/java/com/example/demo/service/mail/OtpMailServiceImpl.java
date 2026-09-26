package com.example.demo.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Year;

@Service("otpMailService")
@RequiredArgsConstructor
public class OtpMailServiceImpl implements OtpMailService {

    private static final Logger log = LoggerFactory.getLogger(OtpMailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    @Value("${app.mail.sender-name:TVR-OTP}")
    private String senderName;

    @Value("${app.mail.sender-email:${spring.mail.username:tharyvireak171@gmail.com}}")
    private String senderEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otp, int expirationMinutes) {
        log.info("Dispatching OTP email to: {}", toEmail);
        String subject = "Here is your spring boot verification code - " + senderName;
        String htmlContent = buildOtpHtml(toEmail, otp, expirationMinutes);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Override
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            try {
                helper.setFrom(new InternetAddress(senderEmail, senderName, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(senderEmail);
            }

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Attach email.png logo inline as cid:emailLogo
            try {
                Resource logoResource = resourceLoader.getResource("classpath:static/email.png");
                if (logoResource.exists()) {
                    helper.addInline("emailLogo", logoResource, "image/png");
                    log.debug("Attached static/email.png inline as cid:emailLogo");
                }
            } catch (Exception ex) {
                log.warn("Could not attach static/email.png inline: {}", ex.getMessage());
            }

            mailSender.send(mimeMessage);
            log.info("Email successfully dispatched to [{}] with subject [{}]", toEmail, subject);

        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private String buildOtpHtml(String recipientEmail, String otp, int expirationMinutes) {
        String template = loadOtpTemplate();
        return template
                .replace("{{appName}}", senderName)
                .replace("{{recipientEmail}}", recipientEmail)
                .replace("{{otp}}", otp)
                .replace("{{expirationMinutes}}", String.valueOf(expirationMinutes))
                .replace("{{currentYear}}", String.valueOf(Year.now().getValue()));
    }

    private String loadOtpTemplate() {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/sent-otp.html");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.error("Failed to read templates/sent-otp.html from classpath: {}", e.getMessage(), e);
            throw new RuntimeException("Could not load email template templates/sent-otp.html", e);
        }
        throw new IllegalStateException("Email template templates/sent-otp.html not found on classpath");
    }
}
