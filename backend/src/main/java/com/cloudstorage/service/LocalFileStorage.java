package com.cloudstorage.service;

import com.cloudstorage.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores file bytes on local disk under app.storage.root, keyed by a random id.
 * Kept behind a small interface-like surface (store/load/delete) so it can be
 * swapped for an S3-backed implementation in a later phase without touching
 * FileService or the controllers.
 */
@Component
public class LocalFileStorage {

    private final Path root;

    public LocalFileStorage(@Value("${app.storage.root}") String root) {
        this.root = Paths.get(root).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize storage directory: " + this.root, e);
        }
    }

    /** Persists the upload and returns a storage key used later to load/delete it. */
    public String store(MultipartFile file, String ownerId) {
        String key = ownerId + "/" + UUID.randomUUID();
        Path target = root.resolve(key).normalize();

        if (!target.startsWith(root)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the uploaded file");
        }

        return key;
    }

    public InputStream load(String storageKey) {
        try {
            Path path = root.resolve(storageKey).normalize();
            if (!path.startsWith(root) || !Files.exists(path)) {
                throw new ApiException(HttpStatus.NOT_FOUND, "File content not found");
            }
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read the file");
        }
    }

    public void delete(String storageKey) {
        try {
            Path path = root.resolve(storageKey).normalize();
            if (path.startsWith(root)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // best-effort cleanup; metadata row is the source of truth
        }
    }
}
