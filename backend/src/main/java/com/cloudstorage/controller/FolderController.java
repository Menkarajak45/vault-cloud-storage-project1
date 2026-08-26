package com.cloudstorage.controller;

import com.cloudstorage.dto.FileDtos.BrowseResponse;
import com.cloudstorage.dto.FolderDtos.*;
import com.cloudstorage.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    private String uid(Authentication auth) {
        return (String) auth.getPrincipal();
    }

    /** Browse a folder's contents. Omit folderId (or pass none) to browse the root ("My Drive"). */
    @GetMapping
    public ResponseEntity<BrowseResponse> browse(Authentication auth,
                                                  @RequestParam(required = false) String folderId) {
        return ResponseEntity.ok(folderService.browse(uid(auth), folderId));
    }

    @PostMapping
    public ResponseEntity<FolderResponse> create(Authentication auth, @Valid @RequestBody CreateFolderRequest request) {
        return ResponseEntity.ok(folderService.create(uid(auth), request));
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<FolderResponse> rename(Authentication auth, @PathVariable String id,
                                                  @Valid @RequestBody RenameRequest request) {
        return ResponseEntity.ok(folderService.rename(uid(auth), id, request));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<FolderResponse> move(Authentication auth, @PathVariable String id,
                                                @RequestBody MoveRequest request) {
        return ResponseEntity.ok(folderService.move(uid(auth), id, request));
    }

    @PatchMapping("/{id}/star")
    public ResponseEntity<FolderResponse> star(Authentication auth, @PathVariable String id,
                                                @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(folderService.star(uid(auth), id, body.getOrDefault("starred", true)));
    }

    @PostMapping("/{id}/trash")
    public ResponseEntity<Void> trash(Authentication auth, @PathVariable String id) {
        folderService.trash(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<FolderResponse> restore(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(folderService.restore(uid(auth), id));
    }

    @GetMapping("/trash")
    public ResponseEntity<List<FolderResponse>> trashList(Authentication auth) {
        return ResponseEntity.ok(folderService.listTrashed(uid(auth)));
    }

    @GetMapping("/starred")
    public ResponseEntity<List<FolderResponse>> starred(Authentication auth) {
        return ResponseEntity.ok(folderService.listStarred(uid(auth)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FolderResponse>> search(Authentication auth, @RequestParam String q) {
        return ResponseEntity.ok(folderService.search(uid(auth), q));
    }
}
