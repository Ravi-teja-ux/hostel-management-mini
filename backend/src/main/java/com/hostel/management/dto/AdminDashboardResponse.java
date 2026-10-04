package com.hostel.management.dto;

public record AdminDashboardResponse(
        long totalStudents,
        long totalRooms,
        long occupiedRooms,
        long availableRooms,
        long pendingLeaveRequests) {
}
