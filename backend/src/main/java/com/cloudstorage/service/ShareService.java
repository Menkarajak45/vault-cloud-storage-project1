package com.cloudstorage.service;

import com.cloudstorage.dto.ShareDtos.*;
import com.cloudstorage.exception.ApiException;
import com.cloudstorage.model.FileItem;
import com.cloudstorage.model.Permission;
import com.cloudstorage.model.Share;
import com.cloudstorage.model.User;
import com.cloudstorage.repository.ShareRepository;
import com.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shareRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @Transactional
    public ShareResponse createOrUpdateShare(String ownerId, CreateShareRequest request) {
        FileItem file = fileService.requireOwnedFile(ownerId, request.fileId());

        Permission permission = parsePermission(request.permission());

        User target = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No user found with that email"));

        if (target.getId().equals(ownerId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You already own this file");
        }

        Share share = shareRepository.findByFileIdAndSharedWithUserId(file.getId(), target.getId())
                .orElseGet(() -> Share.builder()
                        .fileId(file.getId())
                        .ownerId(ownerId)
                        .sharedWithUserId(target.getId())
                        .build());

        share.setPermission(permission);
        share = shareRepository.save(share);

        return toShareResponse(share, file, target);
    }

    @Transactional(readOnly = true)
    public List<ShareResponse> listSharesForFile(String ownerId, String fileId) {
        FileItem file = fileService.requireOwnedFile(ownerId, fileId);
        return shareRepository.findByFileId(fileId).stream()
                .map(share -> {
                    User target = userRepository.findById(share.getSharedWithUserId())
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
                    return toShareResponse(share, file, target);
                })
                .toList();
    }

    @Transactional
    public void revokeShare(String ownerId, String shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Share not found"));

        // Re-validates ownership of the underlying file, not just trust in the caller.
        fileService.requireOwnedFile(ownerId, share.getFileId());

        shareRepository.delete(share);
    }

    @Transactional(readOnly = true)
    public List<SharedWithMeResponse> listSharedWithMe(String userId) {
        return shareRepository.findBySharedWithUserId(userId).stream()
                .map(share -> {
                    FileItem file = fileService.getFileOrThrow(share.getFileId());
                    User owner = userRepository.findById(share.getOwnerId())
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Owner not found"));
                    return new SharedWithMeResponse(
                            share.getId(),
                            fileService.toPublicResponse(file),
                            owner.getEmail(),
                            share.getPermission().name(),
                            share.getCreatedAt()
                    );
                })
                .filter(response -> !response.file().trashed()) // owner's trash isn't the sharee's business
                .toList();
    }

    private Permission parsePermission(String raw) {
        try {
            return Permission.valueOf(raw.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "permission must be EDITOR or VIEWER");
        }
    }

    private ShareResponse toShareResponse(Share share, FileItem file, User target) {
        return new ShareResponse(
                share.getId(), file.getId(), file.getName(),
                target.getId(), target.getEmail(), target.getName(),
                share.getPermission().name(), share.getCreatedAt()
        );
    }
}
