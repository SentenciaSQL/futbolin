package com.futbolin.core.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Redis redis,
        Jwt jwt,
        OAuth oauth,
        Match match,
        Cors cors,
        Seed seed
) {
    public record Redis(boolean enabled) {}

    public record Jwt(String secret, long accessTokenMinutes, long refreshTokenDays) {}

    public record OAuth(String googleClientId, String appleAudience) {}

    public record Match(
            int questionSeconds,
            int durationSeconds,
            int goalsToWin,
            int reconnectSeconds,
            int minAnswerMillis,
            int matchmakingTimeoutSeconds,
            int penaltyKicks
    ) {}

    public record Cors(String allowedOrigins) {}

    public record Seed(String adminEmail, String adminPassword, String adminUsername) {}
}
