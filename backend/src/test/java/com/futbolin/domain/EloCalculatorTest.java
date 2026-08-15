package com.futbolin.domain;

import com.futbolin.domain.ranking.Division;
import com.futbolin.domain.ranking.EloCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EloCalculatorTest {

    private final EloCalculator elo = new EloCalculator();

    @Test
    void winnerGainsPointsAndLoserLosesPoints() {
        EloCalculator.Result result = elo.calculate(1200, 1200, 1.0, 20, 20, 0.7, 0.4);
        assertTrue(result.deltaA() > 0);
        assertTrue(result.deltaB() < 0);
        assertEquals(result.newA(), 1200 + result.deltaA());
    }

    @Test
    void beatingHigherRatedOpponentYieldsBiggerGain() {
        EloCalculator.Result upset = elo.calculate(1100, 1500, 1.0, 30, 30, 0.6, 0.4);
        EloCalculator.Result expected = elo.calculate(1500, 1100, 1.0, 30, 30, 0.6, 0.4);
        assertTrue(upset.deltaA() > expected.deltaA());
    }

    @Test
    void drawMovesPointsTowardEquilibrium() {
        EloCalculator.Result result = elo.calculate(1400, 1000, 0.5, 40, 40, 0.5, 0.5);
        assertTrue(result.deltaA() < 0);
        assertTrue(result.deltaB() > 0);
    }

    @Test
    void divisionsFollowThresholds() {
        assertEquals(Division.AMATEUR, Division.fromPoints(900));
        assertEquals(Division.BRONZE, Division.fromPoints(1000));
        assertEquals(Division.GOLD, Division.fromPoints(1400));
        assertEquals(Division.LEGEND, Division.fromPoints(2300));
    }

    @Test
    void expectedScoreIsSymmetric() {
        assertEquals(0.5, elo.expected(1200, 1200), 0.0001);
        assertEquals(1.0, elo.expected(1200, 800) + elo.expected(800, 1200), 0.0001);
    }
}
