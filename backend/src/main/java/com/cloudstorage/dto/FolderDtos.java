package com.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class FolderDtos {

    public record CreateFolderRequest(
            @NotBlank(message = "Folder name is required") String name,
            String parentId
    ) {}

    public record RenameRequest(
            @NotBlank(message = "Name is required") String name
    ) {}

    public record MoveRequest(
            String targetFolderId // null = move to root
    ) {}

    public record FolderResponse(
            String id,
            String name,
            String parentId,
            boolean starred,
            boolean trashed,
            Instant createdAt
    ) {}
}
