package com.cloudstorage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank(message = "Name is required") String name,
            @NotBlank @Email(message = "A valid email is required") String email,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record ForgotPasswordRequest(
            @NotBlank @Email(message = "A valid email is required") String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
    ) {}

    public record AuthResponse(
            String token,
            UserResponse user
    ) {}

    public record UserResponse(
            String id,
            String name,
            String email,
            String role,
            boolean enabled,
            long storageQuotaBytes,
            long storageUsedBytes
    ) {}
}
