package com.futbolin.data.repository;

import com.futbolin.data.entity.DailyChallengeAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DailyChallengeAnswerRepository extends JpaRepository<DailyChallengeAnswerEntity, UUID> {
    Optional<DailyChallengeAnswerEntity> findByChallengeIdAndUserId(UUID challengeId, UUID userId);
}
