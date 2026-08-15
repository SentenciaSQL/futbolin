package com.futbolin.data.repository;

import com.futbolin.data.entity.MatchRoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRoundRepository extends JpaRepository<MatchRoundEntity, UUID> {
    List<MatchRoundEntity> findByMatchIdOrderByRoundNumberAsc(UUID matchId);
    Optional<MatchRoundEntity> findTopByMatchIdOrderByRoundNumberDesc(UUID matchId);
}
