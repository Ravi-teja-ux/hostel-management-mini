package com.hostel.management.dto;

import java.math.BigDecimal;

public record FeeResponse(
        Long id,
        String studentId,
        String studentName,
        BigDecimal totalFee,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        String paymentStatus) {
}
