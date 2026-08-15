package com.futbolin.domain;

import com.futbolin.domain.tournament.TournamentBracket;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TournamentBracketTest {

    @Test
    void roundOf16HasEightUniquePairings() {
        List<TournamentBracket.Pairing> pairs = TournamentBracket.roundOf16();
        assertEquals(8, pairs.size());
        Set<Integer> seeds = new HashSet<>();
        Set<Integer> slots = new HashSet<>();
        for (TournamentBracket.Pairing pairing : pairs) {
            seeds.add(pairing.seedA());
            seeds.add(pairing.seedB());
            slots.add(pairing.slot());
        }
        assertEquals(16, seeds.size());
        assertEquals(8, slots.size());
        assertEquals(1, pairs.get(0).seedA());
        assertEquals(16, pairs.get(0).seedB());
    }

    @Test
    void winnersAdvanceToTheNextSlot() {
        TournamentBracket.Advancement qf = TournamentBracket.next(TournamentBracket.Round.R16, 3);
        assertEquals(TournamentBracket.Round.QF, qf.nextRound());
        assertEquals(1, qf.nextSlot());
        assertEquals(1, qf.side());

        TournamentBracket.Advancement finalMatch = TournamentBracket.next(TournamentBracket.Round.SF, 1);
        assertEquals(TournamentBracket.Round.F, finalMatch.nextRound());
        assertEquals(0, finalMatch.nextSlot());
        assertEquals(1, finalMatch.side());

        assertNull(TournamentBracket.next(TournamentBracket.Round.F, 0));
    }

    @Test
    void requiresExactlySixteenPlayers() {
        assertThrows(IllegalArgumentException.class, () -> TournamentBracket.requireFull(List.of()));
    }
}
