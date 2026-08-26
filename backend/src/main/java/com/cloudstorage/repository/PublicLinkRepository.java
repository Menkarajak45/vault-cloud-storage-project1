package com.cloudstorage.repository;

import com.cloudstorage.model.PublicLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicLinkRepository extends JpaRepository<PublicLink, String> {
    Optional<PublicLink> findByToken(String token);
    List<PublicLink> findByFileId(String fileId);
}
