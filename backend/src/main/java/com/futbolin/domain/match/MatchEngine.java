package com.futbolin.domain.match;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side authority for pitch movement, possession, goals and penalty shootouts.
 * Client timestamps are never used; only {@link SubmittedAnswer#responseMs()} computed by the server.
 */
public final class MatchEngine {

    public RoundResolution resolve(LiveMatchState state, List<SubmittedAnswer> answers, boolean timeExpired) {
        if (state.phase() == PitchPhase.FINISHED) {
            return fromState(state, List.of("MATCH_ALREADY_FINISHED"), null, false, null);
        }
        if (state.phase() == PitchPhase.PENALTIES || state.phase() == PitchPhase.SUDDEN_DEATH) {
            return resolvePenalty(state, answers);
        }
        Optional<SubmittedAnswer> winner = fastestCorrect(answers);
        List<String> events = new ArrayList<>();
        events.add("ANSWER_RESULT");

        if (state.phase() == PitchPhase.GOAL_CHANCE) {
            if (winner.isPresent() && winner.get().userId().equals(state.possessionUserId())) {
                return scoreGoal(state, winner.get().userId(), events, timeExpired);
            }
            UUID recoveredBy = winner.map(SubmittedAnswer::userId).orElseGet(() -> state.opponent(state.possessionUserId()));
            events.add("POSSESSION_CHANGED");
            events.add("BALL_MOVED");
            int recovered = pullBack(state.ballPosition(), recoveredBy, state.playerA());
            return checkTimeOrGoals(state, recovered, recoveredBy, PitchPhase.OPEN_PLAY,
                    state.scoreA(), state.scoreB(), events, recoveredBy, false, null, timeExpired);
        }

        if (winner.isEmpty()) {
            events.add("POSSESSION_HELD");
            return checkTimeOrGoals(state, state.ballPosition(), state.possessionUserId(),
                    state.phase(),
                    state.scoreA(), state.scoreB(), events, null, false, null, timeExpired);
        }

        UUID winnerId = winner.get().userId();
        boolean attacking = winnerId.equals(state.possessionUserId());

        if (!attacking) {
            events.add("POSSESSION_CHANGED");
            int next = advanceTowardOpponent(state.ballPosition(), winnerId, state.playerA());
            PitchPhase nextPhase = goalChancePhase(next, winnerId, state.playerA());
            if (nextPhase == PitchPhase.GOAL_CHANCE) {
                events.add("GOAL_CHANCE");
            } else {
                events.add("BALL_MOVED");
            }
            return checkTimeOrGoals(state, next, winnerId, nextPhase,
                    state.scoreA(), state.scoreB(), events, winnerId, false, null, timeExpired);
        }

        int next = advanceTowardOpponent(state.ballPosition(), winnerId, state.playerA());
        PitchPhase nextPhase = goalChancePhase(next, winnerId, state.playerA());
        if (nextPhase == PitchPhase.GOAL_CHANCE && alreadyInShootingZone(state.ballPosition(), winnerId, state.playerA())) {
            events.add("GOAL_CHANCE");
            return checkTimeOrGoals(state, next, winnerId, PitchPhase.GOAL_CHANCE,
                    state.scoreA(), state.scoreB(), events, winnerId, false, null, timeExpired);
        }
        if (nextPhase == PitchPhase.GOAL_CHANCE) {
            events.add("BALL_MOVED");
            events.add("GOAL_CHANCE");
        } else {
            events.add("BALL_MOVED");
        }
        return checkTimeOrGoals(state, next, winnerId, nextPhase,
                state.scoreA(), state.scoreB(), events, winnerId, false, null, timeExpired);
    }

    public RoundResolution expireTime(LiveMatchState state) {
        if (state.scoreA() == state.scoreB()) {
            List<String> events = new ArrayList<>();
            events.add("PENALTIES_STARTED");
            return new RoundResolution(
                    0, state.playerA(), PitchPhase.PENALTIES,
                    state.scoreA(), state.scoreB(), 0, 0, 0,
                    null, null, null, events, false, null
            );
        }
        UUID winner = state.scoreA() > state.scoreB() ? state.playerA() : state.playerB();
        return finish(state, state.ballPosition(), state.possessionUserId(),
                state.scoreA(), state.scoreB(), MatchEndReason.TIME, winner, List.of("MATCH_FINISHED"), false, null);
    }

    public RoundResolution abandon(LiveMatchState state, UUID disconnectedUser) {
        UUID winner = state.opponent(disconnectedUser);
        int scoreA = state.scoreA();
        int scoreB = state.scoreB();
        if (state.isPlayerA(winner) && scoreA <= scoreB) {
            scoreA = scoreB + 1;
        } else if (!state.isPlayerA(winner) && scoreB <= scoreA) {
            scoreB = scoreA + 1;
        }
        return finish(state, state.ballPosition(), winner, scoreA, scoreB,
                MatchEndReason.ABANDON, winner, List.of("PLAYER_DISCONNECTED", "MATCH_FINISHED"), false, null);
    }

    private RoundResolution scoreGoal(LiveMatchState state, UUID scorer, List<String> events, boolean timeExpired) {
        events.add("GOAL");
        events.add("SCORE_UPDATED");
        int scoreA = state.scoreA();
        int scoreB = state.scoreB();
        if (state.isPlayerA(scorer)) {
            scoreA++;
        } else {
            scoreB++;
        }
        UUID kickoffTaker = state.opponent(scorer);
        if (scoreA >= state.rules().goalsToWin() || scoreB >= state.rules().goalsToWin()) {
            UUID winner = scoreA > scoreB ? state.playerA() : state.playerB();
            return finish(state, 0, kickoffTaker, scoreA, scoreB, MatchEndReason.GOALS, winner, events, true, scorer);
        }
        events.add("KICKOFF");
        return checkTimeOrGoals(state, 0, kickoffTaker, PitchPhase.GOAL_CELEBRATION,
                scoreA, scoreB, events, scorer, true, scorer, timeExpired);
    }

    private RoundResolution resolvePenalty(LiveMatchState state, List<SubmittedAnswer> answers) {
        List<String> events = new ArrayList<>();
        events.add("ANSWER_RESULT");
        UUID taker = penaltyTaker(state);
        Optional<SubmittedAnswer> takerAnswer = answers.stream()
                .filter(a -> a.userId().equals(taker))
                .findFirst();
        boolean scored = takerAnswer.isPresent() && takerAnswer.get().correct();
        int pA = state.penaltyScoreA();
        int pB = state.penaltyScoreB();
        if (scored) {
            events.add("GOAL");
            if (state.isPlayerA(taker)) {
                pA++;
            } else {
                pB++;
            }
        }
        int nextIndex = state.penaltyIndex() + 1;
        PitchPhase phase = state.phase();
        MatchEndReason reason = null;
        UUID winner = null;

        if (phase == PitchPhase.PENALTIES) {
            Integer decided = penaltyWinnerAfter(nextIndex, pA, pB, state.rules().penaltyKicks(), state);
            if (decided != null) {
                if (decided == 0) {
                    phase = PitchPhase.SUDDEN_DEATH;
                    events.add("SUDDEN_DEATH");
                } else {
                    winner = decided > 0 ? state.playerA() : state.playerB();
                    reason = MatchEndReason.PENALTIES;
                    phase = PitchPhase.FINISHED;
                    events.add("MATCH_FINISHED");
                }
            }
        } else {
            if (nextIndex % 2 == 0 && pA != pB) {
                winner = pA > pB ? state.playerA() : state.playerB();
                reason = MatchEndReason.SUDDEN_DEATH;
                phase = PitchPhase.FINISHED;
                events.add("MATCH_FINISHED");
            }
        }

        UUID nextPossession = penaltyTakerForIndex(state, nextIndex);
        return new RoundResolution(
                0, nextPossession, phase,
                state.scoreA(), state.scoreB(), nextIndex, pA, pB,
                reason, winner, taker, events, scored, scored ? taker : null
        );
    }

    private UUID penaltyTaker(LiveMatchState state) {
        return penaltyTakerForIndex(state, state.penaltyIndex());
    }

    private UUID penaltyTakerForIndex(LiveMatchState state, int index) {
        return index % 2 == 0 ? state.playerA() : state.playerB();
    }

    /**
     * @return 1 if A already won, -1 if B already won, 0 if remaining kicks cannot change a tie after 5 each, null if still running
     */
    Integer penaltyWinnerAfter(int kicksTaken, int pA, int pB, int kicksEach, LiveMatchState state) {
        int remainingA = remainingFor(kicksTaken, kicksEach, true);
        int remainingB = remainingFor(kicksTaken, kicksEach, false);
        if (pA > pB + remainingB) {
            return 1;
        }
        if (pB > pA + remainingA) {
            return -1;
        }
        if (kicksTaken >= kicksEach * 2) {
            return pA == pB ? 0 : (pA > pB ? 1 : -1);
        }
        return null;
    }

    private int remainingFor(int kicksTaken, int kicksEach, boolean playerA) {
        int taken = playerA ? (kicksTaken + 1) / 2 : kicksTaken / 2;
        return Math.max(0, kicksEach - taken);
    }

    private Optional<SubmittedAnswer> fastestCorrect(List<SubmittedAnswer> answers) {
        return answers.stream()
                .filter(SubmittedAnswer::correct)
                .min(Comparator.comparingInt(SubmittedAnswer::responseMs));
    }

    private int advanceTowardOpponent(int position, UUID winner, UUID playerA) {
        int dir = winner.equals(playerA) ? 1 : -1;
        return Math.max(-2, Math.min(2, position + dir));
    }

    private int pullBack(int position, UUID newPossessor, UUID playerA) {
        int dir = newPossessor.equals(playerA) ? 1 : -1;
        return Math.max(-2, Math.min(2, position + dir));
    }

    private boolean alreadyInShootingZone(int position, UUID attacker, UUID playerA) {
        if (attacker.equals(playerA)) {
            return position >= PitchZone.GOAL_B.position();
        }
        return position <= PitchZone.GOAL_A.position();
    }

    private PitchPhase goalChancePhase(int position, UUID possessor, UUID playerA) {
        if (possessor.equals(playerA) && position >= PitchZone.GOAL_B.position()) {
            return PitchPhase.GOAL_CHANCE;
        }
        if (!possessor.equals(playerA) && position <= PitchZone.GOAL_A.position()) {
            return PitchPhase.GOAL_CHANCE;
        }
        return PitchPhase.OPEN_PLAY;
    }

    private RoundResolution checkTimeOrGoals(
            LiveMatchState state,
            int position,
            UUID possession,
            PitchPhase phase,
            int scoreA,
            int scoreB,
            List<String> events,
            UUID roundWinner,
            boolean goal,
            UUID scorer,
            boolean timeExpired
    ) {
        if (scoreA >= state.rules().goalsToWin() || scoreB >= state.rules().goalsToWin()) {
            UUID winner = scoreA > scoreB ? state.playerA() : state.playerB();
            return finish(state, position, possession, scoreA, scoreB, MatchEndReason.GOALS, winner, events, goal, scorer);
        }
        if (timeExpired && phase != PitchPhase.GOAL_CHANCE) {
            LiveMatchState clone = copyScores(state, scoreA, scoreB, position, possession, phase);
            return expireTime(clone);
        }
        return new RoundResolution(
                position, possession, phase, scoreA, scoreB,
                state.penaltyIndex(), state.penaltyScoreA(), state.penaltyScoreB(),
                null, null, roundWinner, events, goal, scorer
        );
    }

    private LiveMatchState copyScores(LiveMatchState state, int scoreA, int scoreB, int position, UUID possession, PitchPhase phase) {
        LiveMatchState copy = new LiveMatchState(state.matchId(), state.playerA(), state.playerB(), state.rules());
        copy.applyRound(new RoundResolution(
                position, possession, phase, scoreA, scoreB,
                0, 0, 0, null, null, null, List.of(), false, null
        ));
        return copy;
    }

    private RoundResolution finish(
            LiveMatchState state,
            int position,
            UUID possession,
            int scoreA,
            int scoreB,
            MatchEndReason reason,
            UUID winner,
            List<String> events,
            boolean goal,
            UUID scorer
    ) {
        if (!events.contains("MATCH_FINISHED")) {
            events = new ArrayList<>(events);
            events.add("MATCH_FINISHED");
        }
        return new RoundResolution(
                position, possession, PitchPhase.FINISHED, scoreA, scoreB,
                state.penaltyIndex(), state.penaltyScoreA(), state.penaltyScoreB(),
                reason, winner, winner, events, goal, scorer
        );
    }

    private RoundResolution fromState(LiveMatchState state, List<String> events, UUID roundWinner, boolean goal, UUID scorer) {
        return new RoundResolution(
                state.ballPosition(), state.possessionUserId(), state.phase(),
                state.scoreA(), state.scoreB(), state.penaltyIndex(),
                state.penaltyScoreA(), state.penaltyScoreB(),
                state.endReason(), state.winnerId(), roundWinner, events, goal, scorer
        );
    }
}
