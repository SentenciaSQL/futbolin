package com.futbolin.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "match_players")
public class MatchPlayerEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private MatchEntity match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String slot;

    @Column(name = "rating_before")
    private Integer ratingBefore;
    @Column(name = "rating_after")
    private Integer ratingAfter;
    @Column(name = "rating_delta")
    private Integer ratingDelta;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    @Column(name = "wrong_answers", nullable = false)
    private int wrongAnswers;

    @Column(nullable = false)
    private int goals;

    @Column(name = "average_answer_ms")
    private Integer averageAnswerMs;

    @Column(name = "xp_earned", nullable = false)
    private int xpEarned;

    @Column(name = "coins_earned", nullable = false)
    private int coinsEarned;

    @Column(nullable = false)
    private boolean muted;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
