package com.cloudstorage.service;

import com.cloudstorage.dto.FileDtos.BrowseResponse;
import com.cloudstorage.dto.FileDtos.Breadcrumb;
import com.cloudstorage.dto.FolderDtos.*;
import com.cloudstorage.exception.ApiException;
import com.cloudstorage.model.FileItem;
import com.cloudstorage.model.Folder;
import com.cloudstorage.repository.FileRepository;
import com.cloudstorage.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    public FolderResponse create(String userId, CreateFolderRequest request) {
        if (request.name() == null || request.name().isBlank() || request.name().trim().length() > 255) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Folder name must be 1-255 characters");
        }
        if (request.parentId() != null) {
            requireOwnedFolder(userId, request.parentId());
        }

        Folder folder = Folder.builder()
                .name(request.name().trim())
                .ownerId(userId)
                .parentId(request.parentId())
                .build();

        return toResponse(folderRepository.save(folder));
    }

    @Transactional(readOnly = true)
    public BrowseResponse browse(String userId, String folderId) {
        if (folderId != null) {
            requireOwnedFolder(userId, folderId);
        }

        List<Folder> folders = folderRepository.findByOwnerIdAndParentIdAndTrashedFalse(userId, folderId);
        List<FileItem> files = fileRepository.findByOwnerIdAndFolderIdAndTrashedFalse(userId, folderId);

        return new BrowseResponse(
                folders.stream().map(this::toResponse).toList(),
                files.stream().map(this::toFileResponse).toList(),
                buildBreadcrumbs(userId, folderId)
        );
    }

    public FolderResponse rename(String userId, String folderId, RenameRequest request) {
        Folder folder = requireOwnedFolder(userId, folderId);
        folder.setName(request.name().trim());
        return toResponse(folderRepository.save(folder));
    }

    public FolderResponse move(String userId, String folderId, MoveRequest request) {
        Folder folder = requireOwnedFolder(userId, folderId);

        if (request.targetFolderId() != null) {
            requireOwnedFolder(userId, request.targetFolderId());
            if (request.targetFolderId().equals(folderId)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "A folder cannot be moved into itself");
            }
            String current = request.targetFolderId();
            int guard = 0;
            while (current != null && guard++ < 100) {
                if (current.equals(folderId)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "A folder cannot be moved inside its own descendant");
                }
                Folder parent = folderRepository.findById(current).orElse(null);
                current = parent == null ? null : parent.getParentId();
            }
        }

        folder.setParentId(request.targetFolderId());
        return toResponse(folderRepository.save(folder));
    }

    public FolderResponse star(String userId, String folderId, boolean starred) {
        Folder folder = requireOwnedFolder(userId, folderId);
        folder.setStarred(starred);
        return toResponse(folderRepository.save(folder));
    }

    @Transactional
    public void trash(String userId, String folderId) {
        Folder folder = requireOwnedFolder(userId, folderId);
        folder.setTrashed(true);
        folder.setTrashedAt(Instant.now());
        folderRepository.save(folder);
        // Note: MVP scope trashes the folder record only; nested contents remain
        // addressable and are hidden from browse() because their parent is trashed.
    }

    public FolderResponse restore(String userId, String folderId) {
        Folder folder = requireOwnedFolder(userId, folderId);
        folder.setTrashed(false);
        folder.setTrashedAt(null);
        return toResponse(folderRepository.save(folder));
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> listTrashed(String userId) {
        return folderRepository.findByOwnerIdAndTrashedTrue(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> listStarred(String userId) {
        return folderRepository.findByOwnerIdAndStarredTrueAndTrashedFalse(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> search(String userId, String query) {
        return folderRepository.findByOwnerIdAndNameContainingIgnoreCaseAndTrashedFalse(userId, query)
                .stream().map(this::toResponse).toList();
    }

    Folder requireOwnedFolder(String userId, String folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Folder not found"));
        if (!folder.getOwnerId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You don't have access to this folder");
        }
        return folder;
    }

    private List<Breadcrumb> buildBreadcrumbs(String userId, String folderId) {
        List<Breadcrumb> trail = new ArrayList<>();
        String currentId = folderId;
        int guard = 0; // avoid infinite loop on corrupted data
        while (currentId != null && guard++ < 50) {
            Folder folder = folderRepository.findById(currentId).orElse(null);
            if (folder == null || !folder.getOwnerId().equals(userId)) break;
            trail.add(0, new Breadcrumb(folder.getId(), folder.getName()));
            currentId = folder.getParentId();
        }
        trail.add(0, new Breadcrumb(null, "My Drive"));
        return trail;
    }

    private FolderResponse toResponse(Folder folder) {
        return new FolderResponse(
                folder.getId(), folder.getName(), folder.getParentId(),
                folder.isStarred(), folder.isTrashed(), folder.getCreatedAt()
        );
    }

    private com.cloudstorage.dto.FileDtos.FileResponse toFileResponse(FileItem file) {
        return new com.cloudstorage.dto.FileDtos.FileResponse(
                file.getId(), file.getName(), file.getContentType(), file.getSizeBytes(),
                file.getFolderId(), file.isStarred(), file.isTrashed(), file.getCreatedAt()
        );
    }
}
