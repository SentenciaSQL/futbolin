package com.futbolin.domain.ranking;

public enum Division {
    AMATEUR(0),
    BRONZE(1000),
    SILVER(1200),
    GOLD(1400),
    PLATINUM(1600),
    DIAMOND(1800),
    ELITE(2000),
    LEGEND(2200);

    private final int minPoints;

    Division(int minPoints) {
        this.minPoints = minPoints;
    }

    public int minPoints() {
        return minPoints;
    }

    public static Division fromPoints(int points) {
        Division current = AMATEUR;
        for (Division d : values()) {
            if (points >= d.minPoints) {
                current = d;
            }
        }
        return current;
    }
}
