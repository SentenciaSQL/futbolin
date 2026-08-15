package com.futbolin.data.repository;

import com.futbolin.data.entity.AchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<AchievementEntity, UUID> {
    Optional<AchievementEntity> findByCode(String code);
}
