package com.futbolin.data.repository;

import com.futbolin.data.entity.TournamentEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentEntryRepository extends JpaRepository<TournamentEntryEntity, UUID> {
    List<TournamentEntryEntity> findByTournamentIdOrderBySeedAsc(UUID tournamentId);
    Optional<TournamentEntryEntity> findByTournamentIdAndUserId(UUID tournamentId, UUID userId);
    long countByTournamentId(UUID tournamentId);
    boolean existsByTournamentIdAndUserId(UUID tournamentId, UUID userId);
}
