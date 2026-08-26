package com.cloudstorage.service;

import com.cloudstorage.dto.AdminDtos.*;
import com.cloudstorage.exception.ApiException;
import com.cloudstorage.model.FileItem;
import com.cloudstorage.model.Role;
import com.cloudstorage.model.User;
import com.cloudstorage.repository.FileRepository;
import com.cloudstorage.repository.FolderRepository;
import com.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalAdmins = userRepository.countByRole(Role.ADMIN);
        long totalFiles = fileRepository.count();
        long totalFolders = folderRepository.count();
        long totalTrashedFiles = fileRepository.countByTrashedTrue();
        long totalStorageUsedBytes = userRepository.findAll().stream()
                .mapToLong(User::getStorageUsedBytes)
                .sum();

        return new DashboardStats(totalUsers, totalAdmins, totalFiles, totalFolders,
                totalTrashedFiles, totalStorageUsedBytes);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toAdminUserResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(String id) {
        return toAdminUserResponse(requireUser(id));
    }

    public AdminUserResponse updateUserStatus(String id, boolean enabled, String actingAdminId) {
        if (id.equals(actingAdminId) && !enabled) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can't disable your own admin account");
        }

        User user = requireUser(id);
        user.setEnabled(enabled);
        return toAdminUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AdminFileResponse> listFiles(Boolean trashed) {
        Map<String, String> emailById = new HashMap<>();
        userRepository.findAll().forEach(u -> emailById.put(u.getId(), u.getEmail()));

        return fileRepository.findAll().stream()
                .filter(f -> trashed == null || f.isTrashed() == trashed)
                .map(f -> toAdminFileResponse(f, emailById.getOrDefault(f.getOwnerId(), "unknown")))
                .toList();
    }

    // No Activity entity exists yet in this MVP, so this returns an explicit
    // "not implemented" flag rather than fabricating data.
    public ActivityListResponse listActivities() {
        return new ActivityListResponse(List.of(), false);
    }

    private User requireUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.isEnabled(),
                user.getStorageQuotaBytes(), user.getStorageUsedBytes(), user.getCreatedAt(), user.getUpdatedAt()
        );
    }

    private AdminFileResponse toAdminFileResponse(FileItem file, String ownerEmail) {
        return new AdminFileResponse(
                file.getId(), file.getName(), file.getContentType(), file.getSizeBytes(),
                file.getOwnerId(), ownerEmail, file.getFolderId(), file.isStarred(), file.isTrashed(),
                file.getCreatedAt()
        );
    }
}
