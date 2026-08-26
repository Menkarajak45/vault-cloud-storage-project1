package com.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class PublicLinkDtos {

    public record CreatePublicLinkRequest(
            @NotBlank(message = "fileId is required") String fileId,
            String password,          // optional — null/blank means no password
            Integer expiresInHours    // optional — null means never expires
    ) {}

    public record PublicLinkResponse(
            String id,
            String fileId,
            String token,
            boolean hasPassword,
            Instant expiresAt,
            Instant createdAt
    ) {}

    // Metadata shown to an anonymous visitor before they download — never exposes the
    // owner's internal ids or the raw password hash.
    public record PublicLinkPreview(
            String fileName,
            long sizeBytes,
            String contentType,
            boolean requiresPassword,
            boolean expired
    ) {}

    public record UnlockRequest(String password) {}

    public record ChangePasswordRequest(String password) {}
}
