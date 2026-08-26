package com.cloudstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    // Path on local disk where the file bytes actually live
    @Column(nullable = false)
    private String storageKey;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    // null folderId = lives at root ("My Drive")
    @Column(name = "folder_id")
    private String folderId;

    @Builder.Default
    private boolean trashed = false;

    @Builder.Default
    private boolean starred = false;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant trashedAt;
}
