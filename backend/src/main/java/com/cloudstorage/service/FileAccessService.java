package com.cloudstorage.service;

import com.cloudstorage.exception.ApiException;
import com.cloudstorage.model.FileItem;
import com.cloudstorage.model.Permission;
import com.cloudstorage.model.Share;
import com.cloudstorage.repository.ShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Every access check for a file (download, rename, trash, permanently delete, ...) goes
 * through here, so the OWNER > EDITOR > VIEWER rules are defined in exactly one place
 * instead of being re-implemented per endpoint.
 *
 * Enforcement notes:
 * - OWNER: everything, including permanently deleting and managing shares/public links.
 * - EDITOR: can view/download, rename, trash, and restore. Cannot move between folders
 *   (folder trees belong to the owner's structure), cannot star (the "starred" flag is a
 *   single field on FileItem, not per-user, so letting an editor toggle it would silently
 *   change the owner's own starred view), and cannot permanently delete or manage sharing.
 * - VIEWER: read/download only.
 */
@Service
@RequiredArgsConstructor
public class FileAccessService {

    private final ShareRepository shareRepository;

    public enum AccessLevel { OWNER, EDITOR, VIEWER, NONE }

    @Transactional(readOnly = true)
    public AccessLevel resolve(String userId, FileItem file) {
        if (file.getOwnerId().equals(userId)) {
            return AccessLevel.OWNER;
        }
        return shareRepository.findByFileIdAndSharedWithUserId(file.getId(), userId)
                .map(Share::getPermission)
                .map(p -> p == Permission.EDITOR ? AccessLevel.EDITOR : AccessLevel.VIEWER)
                .orElse(AccessLevel.NONE);
    }

    public void requireAtLeastViewer(String userId, FileItem file) {
        if (resolve(userId, file) == AccessLevel.NONE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You don't have access to this file");
        }
    }

    public void requireAtLeastEditor(String userId, FileItem file) {
        AccessLevel level = resolve(userId, file);
        if (level == AccessLevel.NONE || level == AccessLevel.VIEWER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You need edit access to do this");
        }
    }

    public void requireOwner(String userId, FileItem file) {
        if (resolve(userId, file) != AccessLevel.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the file's owner can do this");
        }
    }
}
