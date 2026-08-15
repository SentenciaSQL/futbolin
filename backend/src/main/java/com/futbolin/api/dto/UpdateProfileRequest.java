package com.futbolin.api.dto;

public record UpdateProfileRequest(
        String displayName,
        String country,
        String favoriteTeam,
        String avatarKey,
        String frameKey,
        String titleKey
) {}
