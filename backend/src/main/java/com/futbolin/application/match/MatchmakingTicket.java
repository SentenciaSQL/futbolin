package com.futbolin.application.match;

import com.futbolin.data.entity.UserProfileEntity;

import java.util.UUID;

public record MatchmakingTicket(
        UUID userId,
        int rankingPoints,
        String division,
        String country,
        InstantQueued at,
        int latencyMs
) {
    public static MatchmakingTicket from(UserProfileEntity profile, int latencyMs) {
        return new MatchmakingTicket(
                profile.getUserId(),
                profile.getRankingPoints(),
                profile.getDivision().name(),
                profile.getCountry(),
                InstantQueued.now(),
                latencyMs
        );
    }
}
