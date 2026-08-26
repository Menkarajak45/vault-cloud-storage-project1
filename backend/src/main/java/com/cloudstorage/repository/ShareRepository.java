package com.cloudstorage.repository;

import com.cloudstorage.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, String> {
    Optional<Share> findByFileIdAndSharedWithUserId(String fileId, String sharedWithUserId);
    List<Share> findByFileId(String fileId);
    List<Share> findBySharedWithUserId(String sharedWithUserId);
    void deleteByFileId(String fileId);
}
