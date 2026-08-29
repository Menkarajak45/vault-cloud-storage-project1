package com.cloudstorage.config;

import com.cloudstorage.model.Role;
import com.cloudstorage.model.User;
import com.cloudstorage.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_EMAIL / ADMIN_PASSWORD are not set.");
            return;
        }

        if (adminPassword.length() < 8) {
            log.warn("ADMIN_PASSWORD is too short.");
            return;
        }

        String email = adminEmail.toLowerCase().trim();

        var existingAdmins = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .toList();

        if (!existingAdmins.isEmpty()) {

            User admin = existingAdmins.get(0);

            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);

            log.info("Admin account updated successfully for {}", email);
            return;
        }

        User admin = User.builder()
                .name("Administrator")
                .email(email)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);

        log.info("Created initial admin account for {}", email);
    }
}