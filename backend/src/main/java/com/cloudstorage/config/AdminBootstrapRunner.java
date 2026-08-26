package com.cloudstorage.config;

import com.cloudstorage.model.Role;
import com.cloudstorage.model.User;
import com.cloudstorage.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstraps exactly one ADMIN account on startup, if and only if no ADMIN exists yet.
 *
 * Credentials are never hardcoded — they're read from ADMIN_EMAIL / ADMIN_PASSWORD
 * environment variables (mapped below via app.admin.*). If they aren't set, this
 * simply logs a warning and skips creation; the app still starts normally.
 */
@Slf4j
@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return; // already have one — never overwrite or create a second automatically
        }

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("No admin account exists yet, and ADMIN_EMAIL / ADMIN_PASSWORD are not set. " +
                    "Set them as environment variables and restart to create the first admin.");
            return;
        }

        if (adminPassword.length() < 8) {
            log.warn("ADMIN_PASSWORD is too short (min 8 characters) — skipping admin creation.");
            return;
        }

        User admin = User.builder()
                .name("Administrator")
                .email(adminEmail.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        // Never log the password — only confirm creation happened.
        log.info("Created initial admin account for {}", admin.getEmail());
    }
}
