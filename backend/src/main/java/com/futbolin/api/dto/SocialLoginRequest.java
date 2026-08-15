package com.futbolin.api.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
        @NotBlank String provider,
        @NotBlank String idToken,
        String username
) {}
