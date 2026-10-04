package com.hostel.management.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record FeeUpdateRequest(
        @NotNull(message = "Total fee is required") @DecimalMin(value = "0.0", message = "Total fee cannot be negative") BigDecimal totalFee,
        @NotNull(message = "Paid amount is required") @DecimalMin(value = "0.0", message = "Paid amount cannot be negative") BigDecimal paidAmount) {
}
