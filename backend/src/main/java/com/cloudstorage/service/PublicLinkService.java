package com.cloudstorage.service;

import com.cloudstorage.dto.PublicLinkDtos.*;
import com.cloudstorage.exception.ApiException;
import com.cloudstorage.model.FileItem;
import com.cloudstorage.model.PublicLink;
import com.cloudstorage.repository.PublicLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicLinkService {

    private final PublicLinkRepository publicLinkRepository;
    private final FileService fileService;
    private final LocalFileStorage storage;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PublicLinkResponse create(String ownerId, CreatePublicLinkRequest request) {
        FileItem file = fileService.requireOwnedFile(ownerId, request.fileId());
        if (request.expiresInHours() != null && (request.expiresInHours() < 1 || request.expiresInHours() > 8760)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Expiry must be between 1 and 8760 hours");
        }
        if (request.password() != null && !request.password().isBlank()) {
            String value = request.password().trim();
            if (value.length() < 4 || value.length() > 100) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be between 4 and 100 characters");
            }
        }

        PublicLink link = PublicLink.builder()
                .fileId(file.getId())
                .ownerId(ownerId)
                .token(UUID.randomUUID().toString().replace("-", ""))
                .passwordHash(request.password() != null && !request.password().isBlank()
                        ? passwordEncoder.encode(request.password())
                        : null)
                .expiresAt(request.expiresInHours() != null
                        ? Instant.now().plus(request.expiresInHours(), ChronoUnit.HOURS)
                        : null)
                .build();

        return toResponse(publicLinkRepository.save(link));
    }

    @Transactional(readOnly = true)
    public List<PublicLinkResponse> listForFile(String ownerId, String fileId) {
        fileService.requireOwnedFile(ownerId, fileId);
        return publicLinkRepository.findByFileId(fileId).stream()
                .filter(l -> !l.isRevoked())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PublicLinkResponse changePassword(String ownerId, String linkId, String newPassword) {
        PublicLink link = publicLinkRepository.findById(linkId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Link not found"));
        fileService.requireOwnedFile(ownerId, link.getFileId());

        if (newPassword != null && !newPassword.isBlank()) {
            String value = newPassword.trim();
            if (value.length() < 4 || value.length() > 100) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be between 4 and 100 characters");
            }
            link.setPasswordHash(passwordEncoder.encode(value));
        } else {
            link.setPasswordHash(null);
        }
        return toResponse(publicLinkRepository.save(link));
    }

    @Transactional
    public void revoke(String ownerId, String linkId) {
        PublicLink link = publicLinkRepository.findById(linkId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Link not found"));
        fileService.requireOwnedFile(ownerId, link.getFileId()); // re-validates ownership
        link.setRevoked(true);
        publicLinkRepository.save(link);
    }

    // ---- Anonymous / public access below — no auth, so every method re-validates
    //      expiry and revocation itself rather than trusting the caller. ----

    @Transactional(readOnly = true)
    public PublicLinkPreview preview(String token) {
        PublicLink link = requireActiveLink(token);
        FileItem file = fileService.getFileOrThrow(link.getFileId());

        return new PublicLinkPreview(
                file.getName(), file.getSizeBytes(), file.getContentType(),
                link.getPasswordHash() != null, isExpired(link)
        );
    }

    @Transactional(readOnly = true)
    public boolean unlock(String token, String password) {
        PublicLink link = requireActiveLink(token);
        if (link.getPasswordHash() == null) {
            return true; // no password required
        }
        return password != null && passwordEncoder.matches(password, link.getPasswordHash());
    }

    @Transactional(readOnly = true)
    public FileItem resolveDownload(String token, String password) {
        PublicLink link = requireActiveLink(token);

        if (isExpired(link)) {
            throw new ApiException(HttpStatus.GONE, "This link has expired");
        }
        if (link.getPasswordHash() != null &&
                (password == null || !passwordEncoder.matches(password, link.getPasswordHash()))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }

        return fileService.getFileOrThrow(link.getFileId());
    }

    public InputStream loadBytes(FileItem file) {
        return storage.load(file.getStorageKey());
    }

    private PublicLink requireActiveLink(String token) {
        PublicLink link = publicLinkRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "This link doesn't exist"));
        if (link.isRevoked()) {
            throw new ApiException(HttpStatus.GONE, "This link has been revoked");
        }
        return link;
    }

    private boolean isExpired(PublicLink link) {
        return link.getExpiresAt() != null && link.getExpiresAt().isBefore(Instant.now());
    }

    private PublicLinkResponse toResponse(PublicLink link) {
        return new PublicLinkResponse(
                link.getId(), link.getFileId(), link.getToken(),
                link.getPasswordHash() != null, link.getExpiresAt(), link.getCreatedAt()
        );
    }
}
