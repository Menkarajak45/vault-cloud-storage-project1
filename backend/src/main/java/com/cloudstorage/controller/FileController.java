package com.cloudstorage.controller;

import com.cloudstorage.dto.FileDtos.FileResponse;
import com.cloudstorage.model.FileItem;
import com.cloudstorage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    private String uid(Authentication auth) {
        return (String) auth.getPrincipal();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(Authentication auth,
                                                @RequestParam("file") MultipartFile file,
                                                @RequestParam(required = false) String folderId) {
        return ResponseEntity.ok(fileService.upload(uid(auth), folderId, file));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(Authentication auth, @PathVariable String id) {
        FileItem meta = fileService.getMeta(uid(auth), id);
        InputStreamResource body = new InputStreamResource(fileService.download(uid(auth), id));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(meta.getName()).build().toString())
                .contentLength(meta.getSizeBytes())
                .body(body);
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<FileResponse> rename(Authentication auth, @PathVariable String id,
                                                @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(fileService.rename(uid(auth), id, body.get("name")));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<FileResponse> move(Authentication auth, @PathVariable String id,
                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(fileService.move(uid(auth), id, body.get("targetFolderId")));
    }

    @PatchMapping("/{id}/star")
    public ResponseEntity<FileResponse> star(Authentication auth, @PathVariable String id,
                                              @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(fileService.star(uid(auth), id, body.getOrDefault("starred", true)));
    }

    @PostMapping("/{id}/trash")
    public ResponseEntity<Void> trash(Authentication auth, @PathVariable String id) {
        fileService.trash(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<FileResponse> restore(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(fileService.restore(uid(auth), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForever(Authentication auth, @PathVariable String id) {
        fileService.deletePermanently(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trash")
    public ResponseEntity<List<FileResponse>> trashList(Authentication auth) {
        return ResponseEntity.ok(fileService.listTrashed(uid(auth)));
    }

    @GetMapping("/starred")
    public ResponseEntity<List<FileResponse>> starred(Authentication auth) {
        return ResponseEntity.ok(fileService.listStarred(uid(auth)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileResponse>> search(Authentication auth, @RequestParam String q,
                                                     @RequestParam(required = false) String type,
                                                     @RequestParam(required = false) Boolean starred,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(fileService.search(uid(auth), q, type, starred, page, size));
    }
}
