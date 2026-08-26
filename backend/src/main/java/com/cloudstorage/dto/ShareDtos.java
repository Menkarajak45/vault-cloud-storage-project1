package com.cloudstorage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ShareDtos {

    public record CreateShareRequest(
            @NotBlank(message = "fileId is required") String fileId,
            @NotBlank @Email(message = "A valid email is required") String email,
            @NotNull(message = "permission is required (EDITOR or VIEWER)") String permission
    ) {}

    public record ShareResponse(
            String id,
            String fileId,
            String fileName,
            String sharedWithUserId,
            String sharedWithEmail,
            String sharedWithName,
            String permission,
            Instant createdAt
    ) {}

    public record SharedWithMeResponse(
            String shareId,
            FileDtos.FileResponse file,
            String ownerEmail,
            String permission,
            Instant sharedAt
    ) {}
}
