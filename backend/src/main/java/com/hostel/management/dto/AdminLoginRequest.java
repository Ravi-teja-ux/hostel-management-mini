package com.hostel.management.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = "Admin ID is required") String adminId,
        @NotBlank(message = "Password is required") String password) {
}
