package com.futbolin.domain.match;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mutable live snapshot of a two-player pitch match. Pure domain — no I/O.
 */
public class LiveMatchState {

    private final UUID matchId;
    private final UUID playerA;
    private final UUID playerB;
    private final MatchRules rules;
    private int scoreA;
    private int scoreB;
    private int ballPosition;
    private UUID possessionUserId;
    private PitchPhase phase;
    private int roundNumber;
    private int penaltyIndex;
    private int penaltyScoreA;
    private int penaltyScoreB;
    private MatchEndReason endReason;
    private UUID winnerId;
    private final List<String> eventLog = new ArrayList<>();

    public LiveMatchState(UUID matchId, UUID playerA, UUID playerB, MatchRules rules) {
        this.matchId = matchId;
        this.playerA = playerA;
        this.playerB = playerB;
        this.rules = rules;
        this.ballPosition = 0;
        this.possessionUserId = playerA;
        this.phase = PitchPhase.KICKOFF;
        this.roundNumber = 0;
    }

    public UUID matchId() { return matchId; }
    public UUID playerA() { return playerA; }
    public UUID playerB() { return playerB; }
    public MatchRules rules() { return rules; }
    public int scoreA() { return scoreA; }
    public int scoreB() { return scoreB; }
    public int ballPosition() { return ballPosition; }
    public UUID possessionUserId() { return possessionUserId; }
    public PitchPhase phase() { return phase; }
    public int roundNumber() { return roundNumber; }
    public int penaltyIndex() { return penaltyIndex; }
    public int penaltyScoreA() { return penaltyScoreA; }
    public int penaltyScoreB() { return penaltyScoreB; }
    public MatchEndReason endReason() { return endReason; }
    public UUID winnerId() { return winnerId; }
    public List<String> eventLog() { return List.copyOf(eventLog); }
    public PitchZone zone() { return PitchZone.fromPosition(ballPosition); }

    public boolean isPlayerA(UUID userId) {
        return playerA.equals(userId);
    }

    public UUID opponent(UUID userId) {
        return playerA.equals(userId) ? playerB : playerA;
    }

    public void nextRound() {
        roundNumber++;
        if (phase == PitchPhase.KICKOFF) {
            phase = PitchPhase.OPEN_PLAY;
        }
        if (phase == PitchPhase.GOAL_CELEBRATION) {
            phase = PitchPhase.OPEN_PLAY;
        }
    }

    public void applyRound(RoundResolution resolution) {
        eventLog.addAll(resolution.events());
        this.ballPosition = resolution.ballPosition();
        this.possessionUserId = resolution.possessionUserId();
        this.phase = resolution.phase();
        this.scoreA = resolution.scoreA();
        this.scoreB = resolution.scoreB();
        this.penaltyIndex = resolution.penaltyIndex();
        this.penaltyScoreA = resolution.penaltyScoreA();
        this.penaltyScoreB = resolution.penaltyScoreB();
        this.endReason = resolution.endReason();
        this.winnerId = resolution.winnerId();
    }

    public boolean isFinished() {
        return phase == PitchPhase.FINISHED;
    }

    public RoundSnapshot snapshot() {
        return new RoundSnapshot(
                matchId,
                playerA,
                playerB,
                scoreA,
                scoreB,
                ballPosition,
                possessionUserId,
                phase,
                roundNumber,
                penaltyIndex,
                penaltyScoreA,
                penaltyScoreB,
                endReason,
                winnerId
        );
    }
}
