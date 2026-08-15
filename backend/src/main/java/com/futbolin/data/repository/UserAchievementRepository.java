package com.futbolin.data.repository;

import com.futbolin.data.entity.UserAchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievementEntity, UUID> {
    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);
    List<UserAchievementEntity> findByUserId(UUID userId);
}
