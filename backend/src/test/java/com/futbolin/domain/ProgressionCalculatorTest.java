package com.futbolin.domain;

import com.futbolin.domain.progression.ProgressionCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionCalculatorTest {

    private final ProgressionCalculator calc = new ProgressionCalculator();

    @Test
    void winAwardsMoreXpThanLoss() {
        int win = calc.matchXp(true, false, 2, 8, 3);
        int loss = calc.matchXp(false, false, 2, 8, 3);
        assertTrue(win > loss);
    }

    @Test
    void levelIncreasesWithXp() {
        assertEquals(1, calc.levelForXp(0));
        assertTrue(calc.levelForXp(500) > 1);
        assertTrue(calc.levelForXp(5000) > calc.levelForXp(500));
    }

    @Test
    void coinsNeverDependOnRealMoney() {
        int coins = calc.matchCoins(true, 3, 10);
        assertTrue(coins > 0);
        assertTrue(coins < 200);
    }
}
