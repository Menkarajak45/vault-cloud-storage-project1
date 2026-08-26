package com.cloudstorage.repository;

import com.cloudstorage.model.FileItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileItem, String> {
    List<FileItem> findByOwnerIdAndFolderIdAndTrashedFalse(String ownerId, String folderId);
    List<FileItem> findByOwnerIdAndTrashedTrue(String ownerId);
    List<FileItem> findByOwnerIdAndStarredTrueAndTrashedFalse(String ownerId);
    List<FileItem> findByOwnerIdAndNameContainingIgnoreCaseAndTrashedFalse(String ownerId, String query);
    long countByTrashedTrue();
}
