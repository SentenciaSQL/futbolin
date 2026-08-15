package com.futbolin.domain.match;

import java.util.UUID;

public record RoundSnapshot(
        UUID matchId,
        UUID playerA,
        UUID playerB,
        int scoreA,
        int scoreB,
        int ballPosition,
        UUID possessionUserId,
        PitchPhase phase,
        int roundNumber,
        int penaltyIndex,
        int penaltyScoreA,
        int penaltyScoreB,
        MatchEndReason endReason,
        UUID winnerId
) {}
