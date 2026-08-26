package com.cloudstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "folders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    // null parentId = root folder ("My Drive")
    @Column(name = "parent_id")
    private String parentId;

    @Builder.Default
    private boolean trashed = false;

    @Builder.Default
    private boolean starred = false;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant trashedAt;
}
