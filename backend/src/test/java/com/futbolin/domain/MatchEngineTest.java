package com.futbolin.domain;

import com.futbolin.domain.match.LiveMatchState;
import com.futbolin.domain.match.MatchEngine;
import com.futbolin.domain.match.MatchEndReason;
import com.futbolin.domain.match.MatchRules;
import com.futbolin.domain.match.PitchPhase;
import com.futbolin.domain.match.RoundResolution;
import com.futbolin.domain.match.SubmittedAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchEngineTest {

    private final MatchEngine engine = new MatchEngine();
    private UUID a;
    private UUID b;
    private LiveMatchState state;

    @BeforeEach
    void setUp() {
        a = UUID.randomUUID();
        b = UUID.randomUUID();
        state = new LiveMatchState(UUID.randomUUID(), a, b, MatchRules.standard());
        state.nextRound();
    }

    @Test
    void fastestCorrectAdvancesBallTowardOpponent() {
        RoundResolution r = engine.resolve(state, List.of(
                new SubmittedAnswer(a, "A", true, 400),
                new SubmittedAnswer(b, "B", true, 900)
        ), false);
        state.applyRound(r);
        assertEquals(a, state.possessionUserId());
        assertEquals(1, state.ballPosition());
        assertTrue(r.events().contains("BALL_MOVED"));
    }

    @Test
    void defenderStealChangesPossession() {
        RoundResolution r = engine.resolve(state, List.of(
                new SubmittedAnswer(a, "A", false, 300),
                new SubmittedAnswer(b, "B", true, 350)
        ), false);
        state.applyRound(r);
        assertEquals(b, state.possessionUserId());
        assertTrue(r.events().contains("POSSESSION_CHANGED"));
    }

    @Test
    void threeAdvancesCreateGoalChanceThenGoal() {
        scoreOnce(a);
        assertEquals(1, state.scoreA());
        assertEquals(0, state.ballPosition());
        assertEquals(b, state.possessionUserId());
    }

    @Test
    void missedSittersGiveBallToOpponent() {
        advanceToGoalChance(a);
        assertEquals(PitchPhase.GOAL_CHANCE, state.phase());
        state.nextRound();
        RoundResolution miss = engine.resolve(state, List.of(new SubmittedAnswer(a, "A", false, 400)), false);
        state.applyRound(miss);
        assertEquals(b, state.possessionUserId());
        assertFalse(miss.goalScored());
        assertNotEquals(PitchPhase.GOAL_CHANCE, state.phase());
    }

    @Test
    void firstToThreeGoalsFinishesMatch() {
        scoreOnce(a);
        recoverAndScore(a);
        recoverAndScore(a);
        assertEquals(3, state.scoreA());
        assertEquals(PitchPhase.FINISHED, state.phase());
        assertEquals(MatchEndReason.GOALS, state.endReason());
        assertEquals(a, state.winnerId());
    }

    @Test
    void timeExpiryWithDrawStartsPenalties() {
        RoundResolution r = engine.expireTime(state);
        assertEquals(PitchPhase.PENALTIES, r.phase());
        assertTrue(r.events().contains("PENALTIES_STARTED"));
    }

    @Test
    void abandonAwardsWinToOpponent() {
        RoundResolution r = engine.abandon(state, a);
        assertEquals(b, r.winnerId());
        assertEquals(MatchEndReason.ABANDON, r.endReason());
        assertEquals(PitchPhase.FINISHED, r.phase());
    }

    @Test
    void bothWrongKeepsPossession() {
        UUID possessor = state.possessionUserId();
        int pos = state.ballPosition();
        RoundResolution r = engine.resolve(state, List.of(
                new SubmittedAnswer(a, "A", false, 400),
                new SubmittedAnswer(b, "B", false, 410)
        ), false);
        state.applyRound(r);
        assertEquals(possessor, state.possessionUserId());
        assertEquals(pos, state.ballPosition());
    }

    @Test
    void penaltyShootoutCanFinishAfterFiveEach() {
        state.applyRound(engine.expireTime(state));
        for (int i = 0; i < 10; i++) {
            UUID taker = i % 2 == 0 ? a : b;
            boolean goal = i % 2 == 0;
            RoundResolution r = engine.resolve(state, List.of(new SubmittedAnswer(taker, "A", goal, 400)), false);
            state.applyRound(r);
        }
        assertEquals(PitchPhase.FINISHED, state.phase());
        assertEquals(a, state.winnerId());
        assertEquals(MatchEndReason.PENALTIES, state.endReason());
    }

    private void scoreOnce(UUID scorer) {
        advanceToGoalChance(scorer);
        state.nextRound();
        RoundResolution goal = engine.resolve(state, List.of(new SubmittedAnswer(scorer, "A", true, 400)), false);
        state.applyRound(goal);
        assertTrue(goal.goalScored());
        if (!state.isFinished()) {
            state.nextRound();
        }
    }

    private void recoverAndScore(UUID scorer) {
        if (!scorer.equals(state.possessionUserId())) {
            RoundResolution steal = engine.resolve(state, List.of(new SubmittedAnswer(scorer, "A", true, 400)), false);
            state.applyRound(steal);
            state.nextRound();
        }
        scoreOnce(scorer);
    }

    private void advanceToGoalChance(UUID scorer) {
        int guard = 0;
        while (state.phase() != PitchPhase.GOAL_CHANCE && !state.isFinished() && guard++ < 8) {
            RoundResolution r = engine.resolve(state, List.of(new SubmittedAnswer(scorer, "A", true, 400)), false);
            state.applyRound(r);
            if (state.phase() != PitchPhase.GOAL_CHANCE && !state.isFinished()) {
                state.nextRound();
            }
        }
    }
}
