package com.cloudstorage.controller;

import com.cloudstorage.dto.ShareDtos.*;
import com.cloudstorage.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    private String uid(Authentication auth) {
        return (String) auth.getPrincipal();
    }

    @PostMapping
    public ResponseEntity<ShareResponse> create(Authentication auth, @Valid @RequestBody CreateShareRequest request) {
        return ResponseEntity.ok(shareService.createOrUpdateShare(uid(auth), request));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<ShareResponse>> listForFile(Authentication auth, @PathVariable String fileId) {
        return ResponseEntity.ok(shareService.listSharesForFile(uid(auth), fileId));
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> revoke(Authentication auth, @PathVariable String shareId) {
        shareService.revokeShare(uid(auth), shareId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<List<SharedWithMeResponse>> sharedWithMe(Authentication auth) {
        return ResponseEntity.ok(shareService.listSharedWithMe(uid(auth)));
    }
}
