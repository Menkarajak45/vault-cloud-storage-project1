package com.cloudstorage.controller;

import com.cloudstorage.dto.AdminDtos.*;
import com.cloudstorage.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All routes here are restricted to ROLE_ADMIN by SecurityConfig's
 * .requestMatchers("/api/admin/**").hasRole("ADMIN") — enforced at the
 * framework level, not just by checking a flag in each method.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> dashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> users() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> user(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateStatus(Authentication auth, @PathVariable String id,
                                                            @Valid @RequestBody UpdateStatusRequest request) {
        String actingAdminId = (String) auth.getPrincipal();
        return ResponseEntity.ok(adminService.updateUserStatus(id, request.enabled(), actingAdminId));
    }

    @GetMapping("/files")
    public ResponseEntity<List<AdminFileResponse>> files(@RequestParam(required = false) Boolean trashed) {
        return ResponseEntity.ok(adminService.listFiles(trashed));
    }

    @GetMapping("/activities")
    public ResponseEntity<ActivityListResponse> activities() {
        return ResponseEntity.ok(adminService.listActivities());
    }
}
