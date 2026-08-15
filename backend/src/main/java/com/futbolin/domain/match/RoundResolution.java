package com.futbolin.domain.match;

import java.util.List;
import java.util.UUID;

public record RoundResolution(
        int ballPosition,
        UUID possessionUserId,
        PitchPhase phase,
        int scoreA,
        int scoreB,
        int penaltyIndex,
        int penaltyScoreA,
        int penaltyScoreB,
        MatchEndReason endReason,
        UUID winnerId,
        UUID roundWinnerId,
        List<String> events,
        boolean goalScored,
        UUID scorerId
) {}
