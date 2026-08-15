package com.futbolin.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "missions")
public class MissionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String period;

    @Column(name = "name_es", nullable = false)
    private String nameEs;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "description_es", nullable = false)
    private String descriptionEs;

    @Column(name = "description_en", nullable = false)
    private String descriptionEn;

    @Column(nullable = false)
    private String metric;

    @Column(nullable = false)
    private int target;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(name = "coins_reward", nullable = false)
    private int coinsReward;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
