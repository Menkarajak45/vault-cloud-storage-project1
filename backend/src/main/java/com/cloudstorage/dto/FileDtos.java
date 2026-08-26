package com.cloudstorage.dto;

import java.time.Instant;
import java.util.List;

public class FileDtos {

    public record FileResponse(
            String id,
            String name,
            String contentType,
            long sizeBytes,
            String folderId,
            boolean starred,
            boolean trashed,
            Instant createdAt
    ) {}

    public record BrowseResponse(
            List<FolderDtos.FolderResponse> folders,
            List<FileResponse> files,
            List<Breadcrumb> breadcrumbs
    ) {}

    public record Breadcrumb(String id, String name) {}
}
