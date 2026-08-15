package com.futbolin.data.entity;

import com.futbolin.domain.ranking.Division;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private UserEntity user;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "avatar_key", nullable = false)
    private String avatarKey = "default";

    @Column(name = "frame_key", nullable = false)
    private String frameKey = "default";

    @Column(name = "title_key")
    private String titleKey;
    private String country;
    @Column(name = "favorite_team")
    private String favoriteTeam;

    @Column(nullable = false)
    private int level = 1;

    @Column(nullable = false)
    private long xp = 0;

    @Column(nullable = false)
    private int coins = 100;

    @Column(name = "matches_played", nullable = false)
    private int matchesPlayed = 0;

    @Column(nullable = false)
    private int wins = 0;

    @Column(nullable = false)
    private int losses = 0;

    @Column(nullable = false)
    private int draws = 0;

    @Column(name = "goals_scored", nullable = false)
    private int goalsScored = 0;

    @Column(name = "goals_conceded", nullable = false)
    private int goalsConceded = 0;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers = 0;

    @Column(name = "total_answers", nullable = false)
    private int totalAnswers = 0;

    @Column(name = "best_answer_streak", nullable = false)
    private int bestAnswerStreak = 0;

    @Column(name = "current_answer_streak", nullable = false)
    private int currentAnswerStreak = 0;

    @Column(name = "daily_streak", nullable = false)
    private int dailyStreak = 0;

    @Column(name = "last_daily_claim")
    private LocalDate lastDailyClaim;

    @Column(name = "ranking_points", nullable = false)
    private int rankingPoints = 1000;

    @Column(name = "peak_ranking_points", nullable = false)
    private int peakRankingPoints = 1000;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Division division = Division.AMATEUR;

    @Column(name = "average_answer_ms")
    private Integer averageAnswerMs;

    @Column(name = "survival_best", nullable = false)
    private int survivalBest = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public double accuracy() {
        return totalAnswers == 0 ? 0 : (double) correctAnswers / totalAnswers;
    }

    public double winRate() {
        return matchesPlayed == 0 ? 0 : (double) wins / matchesPlayed;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
