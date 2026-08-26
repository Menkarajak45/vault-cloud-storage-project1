package com.cloudstorage.controller;

import com.cloudstorage.dto.PublicLinkDtos.*;
import com.cloudstorage.model.FileItem;
import com.cloudstorage.service.PublicLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public-links")
@RequiredArgsConstructor
public class PublicLinkController {

    private final PublicLinkService publicLinkService;

    private String uid(Authentication auth) {
        return (String) auth.getPrincipal();
    }

    // ---- Owner-side management (authenticated — see SecurityConfig) ----

    @PostMapping
    public ResponseEntity<PublicLinkResponse> create(Authentication auth,
                                                       @Valid @RequestBody CreatePublicLinkRequest request) {
        return ResponseEntity.ok(publicLinkService.create(uid(auth), request));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<PublicLinkResponse>> listForFile(Authentication auth, @PathVariable String fileId) {
        return ResponseEntity.ok(publicLinkService.listForFile(uid(auth), fileId));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<PublicLinkResponse> changePassword(Authentication auth, @PathVariable String id,
                                                              @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(publicLinkService.changePassword(uid(auth), id, request.password()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(Authentication auth, @PathVariable String id) {
        publicLinkService.revoke(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    // ---- Anonymous access (public — see SecurityConfig for the exact permitAll patterns) ----

    @GetMapping("/{token}/preview")
    public ResponseEntity<PublicLinkPreview> preview(@PathVariable String token) {
        return ResponseEntity.ok(publicLinkService.preview(token));
    }

    @PostMapping("/{token}/unlock")
    public ResponseEntity<Map<String, Boolean>> unlock(@PathVariable String token, @RequestBody UnlockRequest request) {
        boolean ok = publicLinkService.unlock(token, request.password());
        return ResponseEntity.ok(Map.of("unlocked", ok));
    }

    @GetMapping("/{token}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String token,
                                                          @RequestParam(required = false) String password) {
        FileItem file = publicLinkService.resolveDownload(token, password);
        InputStreamResource body = new InputStreamResource(publicLinkService.loadBytes(file));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.getName()).build().toString())
                .contentLength(file.getSizeBytes())
                .body(body);
    }
}
