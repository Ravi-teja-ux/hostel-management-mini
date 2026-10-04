package com.hostel.management.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Student ID is required") String studentId,
        @NotBlank(message = "Password is required") String password) {
}
