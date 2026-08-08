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

    @Value("${spring.mail.username:noreply@pagemind.com}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String code) {
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
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            log.info("LOCAL DEV FALLBACK - Password reset code for {}: {}", toEmail, code);
        }
    }
}
