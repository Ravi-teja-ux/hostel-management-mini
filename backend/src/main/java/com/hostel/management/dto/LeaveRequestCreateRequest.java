package com.hostel.management.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeaveRequestCreateRequest(
        @NotBlank(message = "Student ID is required") String studentId,
        @NotNull(message = "Leave date is required") LocalDate leaveDate,
        @NotNull(message = "Return date is required") LocalDate returnDate,
        @NotBlank(message = "Reason is required") @Size(max = 500, message = "Reason must be 500 characters or fewer") String reason) {
}
