package com.futbolin.data.repository;

import com.futbolin.data.entity.RankingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RankingRepository extends JpaRepository<RankingEntity, UUID> {
    Optional<RankingEntity> findByUserIdAndSeasonId(UUID userId, UUID seasonId);
    Page<RankingEntity> findBySeasonIdOrderByPointsDesc(UUID seasonId, Pageable pageable);
}
