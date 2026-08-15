package com.futbolin.data.repository;

import com.futbolin.data.entity.MatchPlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchPlayerRepository extends JpaRepository<MatchPlayerEntity, UUID> {
    List<MatchPlayerEntity> findByMatchId(UUID matchId);
    Optional<MatchPlayerEntity> findByMatchIdAndUserId(UUID matchId, UUID userId);
}
