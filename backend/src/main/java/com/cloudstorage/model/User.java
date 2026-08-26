package com.cloudstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String passwordHash;

    // Password reset fields. Tokens are short-lived and single-use.
    private String resetToken;
    private Instant resetTokenExpiresAt;

    // Platform-level role (ADMIN / USER). Distinct from per-file OWNER/EDITOR/VIEWER
    // permissions, which live on the sharing model, not here.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    // Admins can disable an account without deleting it. Disabled users fail auth
    // on every protected request (see JwtAuthFilter).
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private Long storageQuotaBytes = 5L * 1024 * 1024 * 1024; // 5GB default quota

    @Builder.Default
    private Long storageUsedBytes = 0L;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
