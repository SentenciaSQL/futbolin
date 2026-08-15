package com.futbolin.domain.progression;

public final class ProgressionCalculator {

    public static final int BASE_LEVEL_XP = 120;

    public int levelForXp(long xp) {
        int level = 1;
        long remaining = xp;
        while (remaining >= xpToNext(level)) {
            remaining -= xpToNext(level);
            level++;
            if (level > 99) {
                return 99;
            }
        }
        return level;
    }

    public long xpToNext(int level) {
        return BASE_LEVEL_XP + (long) (35L * (level - 1) * Math.sqrt(level));
    }

    public int matchXp(boolean win, boolean draw, int goals, int correct, int streak) {
        int xp = 20 + correct * 5 + goals * 15;
        if (win) {
            xp += 50;
        } else if (draw) {
            xp += 20;
        }
        if (streak >= 5) {
            xp += 15;
        }
        return xp;
    }

    public int matchCoins(boolean win, int goals, int correct) {
        int coins = 8 + correct + goals * 3;
        if (win) {
            coins += 12;
        }
        return coins;
    }
}
