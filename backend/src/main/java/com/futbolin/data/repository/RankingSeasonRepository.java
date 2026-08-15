package com.futbolin.data.repository;

import com.futbolin.data.entity.RankingSeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RankingSeasonRepository extends JpaRepository<RankingSeasonEntity, UUID> {
    Optional<RankingSeasonEntity> findByActiveTrue();
    Optional<RankingSeasonEntity> findBySlug(String slug);
}
