package com.cloudstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "shares", uniqueConstraints = @UniqueConstraint(columnNames = {"file_id", "shared_with_user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "shared_with_user_id", nullable = false)
    private String sharedWithUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
