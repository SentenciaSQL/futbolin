package com.futbolin.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "match_answers")
public class MatchAnswerEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id")
    private MatchRoundEntity round;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "option_key")
    private String optionKey;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "response_ms", nullable = false)
    private int responseMs;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
