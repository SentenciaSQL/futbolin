package com.futbolin.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tournament_matches")
public class TournamentMatchEntity {

    @Id
    private UUID id;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column(name = "round_name", nullable = false)
    private String roundName;

    @Column(nullable = false)
    private int slot;

    @Column(name = "player_a_id")
    private UUID playerAId;

    @Column(name = "player_b_id")
    private UUID playerBId;

    @Column(name = "winner_id")
    private UUID winnerId;

    @Column(name = "match_id")
    private UUID matchId;

    @Column(nullable = false)
    private String status;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
