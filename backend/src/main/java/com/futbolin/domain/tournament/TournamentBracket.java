package com.futbolin.domain.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fixed 16-player single-elimination bracket.
 * Seeds 1..16 pair as 1v16, 2v15, 3v14, 4v13, 5v12, 6v11, 7v10, 8v9.
 */
public final class TournamentBracket {

    public static final int SIZE = 16;

    public enum Round {
        R16(8, "Octavos"),
        QF(4, "Cuartos"),
        SF(2, "Semifinal"),
        F(1, "Final");

        private final int matches;
        private final String label;

        Round(int matches, String label) {
            this.matches = matches;
            this.label = label;
        }

        public int matches() {
            return matches;
        }

        public String label() {
            return label;
        }

        public Round next() {
            return switch (this) {
                case R16 -> QF;
                case QF -> SF;
                case SF -> F;
                case F -> null;
            };
        }
    }

    public record Pairing(int slot, int seedA, int seedB) {}

    public record Advancement(Round nextRound, int nextSlot, int side) {}

    private TournamentBracket() {}

    public static List<Pairing> roundOf16() {
        List<Pairing> pairs = new ArrayList<>();
        int[] high = {1, 8, 4, 5, 2, 7, 3, 6};
        int[] low = {16, 9, 13, 12, 15, 10, 14, 11};
        for (int i = 0; i < 8; i++) {
            pairs.add(new Pairing(i, high[i], low[i]));
        }
        return pairs;
    }

    public static Advancement next(Round round, int slot) {
        Round next = round.next();
        if (next == null) {
            return null;
        }
        return new Advancement(next, slot / 2, slot % 2);
    }

    public static void requireFull(List<UUID> seededPlayers) {
        if (seededPlayers == null || seededPlayers.size() != SIZE) {
            throw new IllegalArgumentException("Tournament requires exactly 16 players");
        }
    }
}
