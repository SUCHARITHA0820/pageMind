package com.pagemind.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:pagemind0@gmail.com}")
    private String fromEmail;

    public boolean sendPasswordResetEmail(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("PageMind - Password Reset Code");
            message.setText("Your password reset code is: " + code + "\n\n" +
                    "This code will expire in 10 minutes.\n" +
                    "If you did not request this, please ignore this email.");
            mailSender.send(message);
            log.info("Password reset email sent successfully to {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            log.info("LOCAL DEV FALLBACK - Password reset code for {}: {}", toEmail, code);
            return false;
        }
    }

    public boolean sendWelcomeEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to PageMind! 📚✨");
            message.setText("Hello " + (userName != null && !userName.isBlank() ? userName : "Reader") + ",\n\n" +
                    "Welcome to PageMind! We are thrilled to have you join our book discovery community.\n\n" +
                    "With PageMind, you can:\n" +
                    "• Explore hundreds of curated books across all genres\n" +
                    "• Save your favorite books to your personal profile\n" +
                    "• Chat with our AI Companion for personalized book recommendations\n\n" +
                    "Happy Reading!\n\n" +
                    "Best regards,\n" +
                    "The PageMind Team");
            mailSender.send(message);
            log.info("Welcome notification email sent successfully to {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }
}
