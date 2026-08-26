package com.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends password-reset links. If SMTP is not configured, the link is logged
 * to the backend console so the feature can still be demonstrated locally.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendPasswordReset(String email, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;

        if (from == null || from.isBlank()) {
            System.out.println("[PASSWORD RESET - LOCAL DEMO] " + link);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Reset your Vault password");
            message.setText(
                    "We received a request to reset your Vault password.\n\n" +
                    "Open this link within 30 minutes:\n" + link +
                    "\n\nIf you did not request this, you can ignore this email."
            );
            message.setFrom(from);
            mailSender.send(message);
        } catch (Exception ex) {
            // Do not expose SMTP errors to the client; keep the demo usable.
            System.err.println("[PASSWORD RESET EMAIL FAILED] " + ex.getMessage());
            System.out.println("[PASSWORD RESET - LOCAL DEMO] " + link);
        }
    }
}
