package com.cloudstorage.service;

import com.cloudstorage.dto.FileDtos.FileResponse;
import com.cloudstorage.exception.ApiException;
import com.cloudstorage.model.FileItem;
import com.cloudstorage.model.User;
import com.cloudstorage.repository.FileRepository;
import com.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final LocalFileStorage storage;
    private final FolderService folderService;
    private final FileAccessService fileAccessService;

    @Transactional
    public FileResponse upload(String userId, String folderId, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No file was provided");
        }
        if (folderId != null) {
            folderService.requireOwnedFolder(userId, folderId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        long incomingSize = multipartFile.getSize();
        if (user.getStorageUsedBytes() + incomingSize > user.getStorageQuotaBytes()) {
            throw new ApiException(HttpStatus.INSUFFICIENT_STORAGE, "Storage quota exceeded");
        }

        String key = storage.store(multipartFile, userId);

        FileItem file = FileItem.builder()
                .name(multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "untitled")
                .contentType(multipartFile.getContentType() != null ? multipartFile.getContentType() : "application/octet-stream")
                .sizeBytes(incomingSize)
                .storageKey(key)
                .ownerId(userId)
                .folderId(folderId)
                .build();

        file = fileRepository.save(file);

        user.setStorageUsedBytes(user.getStorageUsedBytes() + incomingSize);
        userRepository.save(user);

        return toResponse(file);
    }

    @Transactional(readOnly = true)
    public FileItem requireOwnedFile(String userId, String fileId) {
        FileItem file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
        if (!file.getOwnerId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You don't have access to this file");
        }
        return file;
    }

    @Transactional(readOnly = true)
    public FileItem getFileOrThrow(String fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
    }

    @Transactional(readOnly = true)
    public InputStream download(String userId, String fileId) {
        FileItem file = getFileOrThrow(fileId);
        fileAccessService.requireAtLeastViewer(userId, file);
        return storage.load(file.getStorageKey());
    }

    @Transactional(readOnly = true)
    public FileItem getMeta(String userId, String fileId) {
        FileItem file = getFileOrThrow(fileId);
        fileAccessService.requireAtLeastViewer(userId, file);
        return file;
    }

    public FileResponse rename(String userId, String fileId, String newName) {
        FileItem file = getFileOrThrow(fileId);
        fileAccessService.requireAtLeastEditor(userId, file);
        file.setName(newName.trim());
        return toResponse(fileRepository.save(file));
    }

    public FileResponse move(String userId, String fileId, String targetFolderId) {
        // Moving between folders is owner-only: the folder tree being moved into
        // belongs to the owner's structure, not the editor's.
        FileItem file = requireOwnedFile(userId, fileId);
        if (targetFolderId != null) {
            folderService.requireOwnedFolder(userId, targetFolderId);
        }
        file.setFolderId(targetFolderId);
        return toResponse(fileRepository.save(file));
    }

    public FileResponse star(String userId, String fileId, boolean starred) {
        // Owner-only: "starred" is a single field on FileItem, not per-viewer, so an
        // editor toggling it would silently change what the owner sees as starred too.
        FileItem file = requireOwnedFile(userId, fileId);
        file.setStarred(starred);
        return toResponse(fileRepository.save(file));
    }

    public void trash(String userId, String fileId) {
        FileItem file = getFileOrThrow(fileId);
        fileAccessService.requireAtLeastEditor(userId, file);
        file.setTrashed(true);
        file.setTrashedAt(Instant.now());
        fileRepository.save(file);
    }

    public FileResponse restore(String userId, String fileId) {
        FileItem file = getFileOrThrow(fileId);
        fileAccessService.requireAtLeastEditor(userId, file);
        file.setTrashed(false);
        file.setTrashedAt(null);
        return toResponse(fileRepository.save(file));
    }

    @Transactional
    public void deletePermanently(String userId, String fileId) {
        // Owner-only: this also frees the owner's storage quota.
        FileItem file = requireOwnedFile(userId, fileId);
        storage.delete(file.getStorageKey());
        fileRepository.delete(file);

        User user = userRepository.findById(userId).orElseThrow();
        user.setStorageUsedBytes(Math.max(0, user.getStorageUsedBytes() - file.getSizeBytes()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listTrashed(String userId) {
        return fileRepository.findByOwnerIdAndTrashedTrue(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listStarred(String userId) {
        return fileRepository.findByOwnerIdAndStarredTrueAndTrashedFalse(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FileResponse> search(String userId, String query, String type, Boolean starred, int page, int size) {
        String q = query == null ? "" : query.trim();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        List<FileItem> results = fileRepository.findByOwnerIdAndNameContainingIgnoreCaseAndTrashedFalse(userId, q);
        if (Boolean.TRUE.equals(starred)) results = results.stream().filter(FileItem::isStarred).toList();
        if (type != null && !type.isBlank()) {
            String t = type.toLowerCase();
            results = results.stream().filter(f -> {
                String ct = f.getContentType() == null ? "" : f.getContentType().toLowerCase();
                return switch (t) {
                    case "image" -> ct.startsWith("image/");
                    case "pdf" -> ct.equals("application/pdf");
                    case "video" -> ct.startsWith("video/");
                    case "audio" -> ct.startsWith("audio/");
                    case "document" -> ct.startsWith("text/") || ct.contains("word") || ct.contains("document") || ct.contains("spreadsheet") || ct.contains("excel");
                    default -> true;
                };
            }).toList();
        }
        int from = Math.min(safePage * safeSize, results.size());
        int to = Math.min(from + safeSize, results.size());
        return results.subList(from, to).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FileResponse> search(String userId, String query) {
        return search(userId, query, null, false, 0, 100);
    }

    private FileResponse toResponse(FileItem file) {
        return new FileResponse(
                file.getId(), file.getName(), file.getContentType(), file.getSizeBytes(),
                file.getFolderId(), file.isStarred(), file.isTrashed(), file.getCreatedAt()
        );
    }

    // Exposed so ShareService can render "shared with me" results using the same shape.
    public FileResponse toPublicResponse(FileItem file) {
        return toResponse(file);
    }
}
