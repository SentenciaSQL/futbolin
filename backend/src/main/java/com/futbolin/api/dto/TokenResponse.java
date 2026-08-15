package com.futbolin.api.dto;

import java.util.UUID;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String username
) {
    public static TokenResponse of(String access, String refresh, long expires, UUID userId, String username) {
        return new TokenResponse(access, refresh, "Bearer", expires, userId, username);
    }
}
