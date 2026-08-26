package com.cloudstorage.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public class AdminDtos {

    public record DashboardStats(
            long totalUsers,
            long totalAdmins,
            long totalFiles,
            long totalFolders,
            long totalTrashedFiles,
            long totalStorageUsedBytes
    ) {}

    public record AdminUserResponse(
            String id,
            String name,
            String email,
            String role,
            boolean enabled,
            long storageQuotaBytes,
            long storageUsedBytes,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record UpdateStatusRequest(
            @NotNull(message = "enabled is required") Boolean enabled
    ) {}

    public record AdminFileResponse(
            String id,
            String name,
            String contentType,
            long sizeBytes,
            String ownerId,
            String ownerEmail,
            String folderId,
            boolean starred,
            boolean trashed,
            Instant createdAt
    ) {}

    // Activity logging isn't implemented yet (no Activity entity in this MVP) — this
    // shape is here so the endpoint contract is stable once it is added.
    public record ActivityResponse(
            String id,
            String userId,
            String action,
            Instant timestamp
    ) {}

    public record ActivityListResponse(
            List<ActivityResponse> activities,
            boolean implemented
    ) {}
}
