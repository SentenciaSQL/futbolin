package com.futbolin.data.repository;

import com.futbolin.data.entity.MatchAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchAnswerRepository extends JpaRepository<MatchAnswerEntity, UUID> {
    List<MatchAnswerEntity> findByRoundId(UUID roundId);
    Optional<MatchAnswerEntity> findByRoundIdAndUserId(UUID roundId, UUID userId);
    boolean existsByRoundIdAndUserId(UUID roundId, UUID userId);
}
