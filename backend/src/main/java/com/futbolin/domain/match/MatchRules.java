package com.futbolin.domain.match;

import java.util.UUID;

public record MatchRules(
        int questionSeconds,
        int durationSeconds,
        int goalsToWin,
        int reconnectSeconds,
        int minAnswerMillis,
        int penaltyKicks
) {
    public static MatchRules standard() {
        return new MatchRules(10, 240, 3, 15, 180, 5);
    }

    public boolean isGoalChanceFor(UUID possessor, UUID playerA, int position) {
        if (possessor.equals(playerA)) {
            return position >= PitchZone.GOAL_B.position();
        }
        return position <= PitchZone.GOAL_A.position();
    }
}
