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
@Table(name = "daily_login_rewards")
public class DailyLoginRewardEntity {

    @Id
    private UUID id;

    @Column(name = "day_index", nullable = false, unique = true)
    private int dayIndex;

    @Column(nullable = false)
    private int coins;

    @Column(nullable = false)
    private int xp;

    @Column(name = "cosmetic_key")
    private String cosmeticKey;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
