package com.hostel.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StudentRequest(
        @NotBlank(message = "Student ID is required") String studentId,
        @Size(min = 6, message = "Password must contain at least 6 characters") String password,
        @NotBlank(message = "Student name is required") String name,
        String course,
        @NotNull(message = "Year is required") @Positive(message = "Year must be positive") Integer year,
        String roomNumber,
        String phone,
        @NotBlank(message = "Status is required") String status) {
}
