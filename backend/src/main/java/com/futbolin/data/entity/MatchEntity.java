package com.futbolin.data.entity;

import com.futbolin.domain.match.MatchEndReason;
import com.futbolin.domain.match.MatchMode;
import com.futbolin.domain.match.MatchStatus;
import com.futbolin.domain.match.PitchPhase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(name = "private_code")
    private String privateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private RankingSeasonEntity season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_a_id")
    private UserEntity playerA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_b_id")
    private UserEntity playerB;

    @Column(name = "score_a", nullable = false)
    private int scoreA;

    @Column(name = "score_b", nullable = false)
    private int scoreB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private UserEntity winner;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason")
    private MatchEndReason endReason;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds = 240;

    @Column(name = "goals_to_win", nullable = false)
    private int goalsToWin = 3;

    @Column(name = "ball_position", nullable = false)
    private int ballPosition;

    @Column(name = "possession_user_id")
    private UUID possessionUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pitch_phase", nullable = false)
    private PitchPhase pitchPhase = PitchPhase.KICKOFF;

    @Column(name = "started_at")
    private Instant startedAt;
    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
