package com.cloudstorage.repository;

import com.cloudstorage.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, String> {
    List<Folder> findByOwnerIdAndParentIdAndTrashedFalse(String ownerId, String parentId);
    List<Folder> findByOwnerIdAndTrashedTrue(String ownerId);
    List<Folder> findByOwnerIdAndStarredTrueAndTrashedFalse(String ownerId);
    List<Folder> findByOwnerIdAndNameContainingIgnoreCaseAndTrashedFalse(String ownerId, String query);
}
