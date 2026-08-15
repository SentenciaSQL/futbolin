package com.futbolin.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "daily_challenge_answers")
public class DailyChallengeAnswerEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id")
    private DailyChallengeEntity challenge;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String optionKey;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (answeredAt == null) {
            answeredAt = Instant.now();
        }
    }
}
