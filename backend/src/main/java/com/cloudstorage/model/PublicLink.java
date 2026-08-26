package com.cloudstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "public_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(nullable = false, unique = true)
    private String token;

    // null = no password required
    private String passwordHash;

    // null = never expires
    private Instant expiresAt;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private boolean revoked = false;
}
