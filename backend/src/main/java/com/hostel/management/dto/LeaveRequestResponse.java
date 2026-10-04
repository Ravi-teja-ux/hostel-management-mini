package com.hostel.management.dto;

import java.time.LocalDate;

public record LeaveRequestResponse(
        Long id,
        String studentId,
        String studentName,
        LocalDate leaveDate,
        LocalDate returnDate,
        String reason,
        String status) {
}
