package com.hostel.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RoomRequest(
        @NotBlank(message = "Room number is required") String roomNumber,
        @NotBlank(message = "Block is required") String block,
        @NotBlank(message = "Room type is required") String roomType,
        @NotNull(message = "Capacity is required") @Positive(message = "Capacity must be positive") Integer capacity,
        @NotNull(message = "Occupied beds is required") @PositiveOrZero(message = "Occupied beds cannot be negative") Integer occupiedBeds) {
}
