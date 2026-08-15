package com.futbolin.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "achievements")
public class AchievementEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "name_es", nullable = false)
    private String nameEs;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "description_es", nullable = false)
    private String descriptionEs;

    @Column(name = "description_en", nullable = false)
    private String descriptionEn;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(name = "coins_reward", nullable = false)
    private int coinsReward;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
