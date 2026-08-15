package com.futbolin.data.repository;

import com.futbolin.data.entity.DailyChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyChallengeRepository extends JpaRepository<DailyChallengeEntity, UUID> {
    Optional<DailyChallengeEntity> findByChallengeDate(LocalDate date);
}
