package com.futbolin.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cosmetics")
public class CosmeticEntity {

    @Id
    private UUID id;

    @Column(name = "cosmetic_key", nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String type;

    @Column(name = "name_es", nullable = false)
    private String nameEs;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private String rarity;

    @Column(name = "price_coins", nullable = false)
    private int priceCoins;

    @Column(name = "min_level", nullable = false)
    private int minLevel;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
