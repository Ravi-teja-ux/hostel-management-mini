package com.hostel.management.dto;

public record LoginResponse(String message, String role, String id, String name, String accessToken, long expiresIn) {
}
