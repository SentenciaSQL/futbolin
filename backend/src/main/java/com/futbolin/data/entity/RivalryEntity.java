package com.futbolin.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "rivalries")
public class RivalryEntity {

    @Id
    private UUID id;

    @Column(name = "user_a_id", nullable = false)
    private UUID userAId;

    @Column(name = "user_b_id", nullable = false)
    private UUID userBId;

    @Column(name = "matches_played", nullable = false)
    private int matchesPlayed;

    @Column(name = "wins_a", nullable = false)
    private int winsA;

    @Column(name = "wins_b", nullable = false)
    private int winsB;

    @Column(nullable = false)
    private int draws;

    @Column(name = "last_match_at")
    private Instant lastMatchAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
