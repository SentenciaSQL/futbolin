package com.futbolin.domain.ranking;

/**
 * Classic Elo with a performance multiplier based on goals and accuracy.
 * Never used as pay-to-win: only match outcome + in-match sporting performance.
 */
public final class EloCalculator {

    public static final int DEFAULT_K = 24;
    public static final int PROVISIONAL_K = 32;
    public static final int STARTING_POINTS = 1000;

    public record Result(int deltaA, int deltaB, int newA, int newB) {}

    public Result calculate(int ratingA, int ratingB, double scoreA, int matchesA, int matchesB, double performanceA, double performanceB) {
        int kA = matchesA < 15 ? PROVISIONAL_K : DEFAULT_K;
        int kB = matchesB < 15 ? PROVISIONAL_K : DEFAULT_K;
        double expectedA = expected(ratingA, ratingB);
        double expectedB = 1.0 - expectedA;
        double adjA = clamp(scoreA + (performanceA - 0.5) * 0.15, 0, 1);
        double adjB = clamp((1.0 - scoreA) + (performanceB - 0.5) * 0.15, 0, 1);
        int deltaA = (int) Math.round(kA * (adjA - expectedA));
        int deltaB = (int) Math.round(kB * (adjB - expectedB));
        if (scoreA == 1 && deltaA < 4) {
            deltaA = 4;
        }
        if (scoreA == 0 && deltaB < 4) {
            deltaB = 4;
        }
        int newA = Math.max(0, ratingA + deltaA);
        int newB = Math.max(0, ratingB + deltaB);
        return new Result(newA - ratingA, newB - ratingB, newA, newB);
    }

    public double expected(int rating, int opponent) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponent - rating) / 400.0));
    }

    /**
     * Performance 0..1 from accuracy and goals share.
     */
    public double performance(int correct, int total, int goals, int goalsForBoth) {
        double accuracy = total == 0 ? 0.5 : (double) correct / total;
        double goalShare = goalsForBoth == 0 ? 0.5 : (double) goals / goalsForBoth;
        return clamp(0.65 * accuracy + 0.35 * goalShare, 0, 1);
    }

    public double matchScore(int scoreSelf, int scoreOpp) {
        if (scoreSelf > scoreOpp) {
            return 1.0;
        }
        if (scoreSelf < scoreOpp) {
            return 0.0;
        }
        return 0.5;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
