package com.futbolin.data.repository;

import com.futbolin.data.entity.TournamentMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentMatchRepository extends JpaRepository<TournamentMatchEntity, UUID> {
    List<TournamentMatchEntity> findByTournamentIdOrderByRoundNameAscSlotAsc(UUID tournamentId);
    Optional<TournamentMatchEntity> findByMatchId(UUID matchId);
    Optional<TournamentMatchEntity> findByTournamentIdAndRoundNameAndSlot(UUID tournamentId, String roundName, int slot);
}
