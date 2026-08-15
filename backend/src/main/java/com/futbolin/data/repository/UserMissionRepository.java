package com.futbolin.data.repository;

import com.futbolin.data.entity.UserMissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMissionRepository extends JpaRepository<UserMissionEntity, UUID> {
    Optional<UserMissionEntity> findByUserIdAndMissionIdAndPeriodKey(UUID userId, UUID missionId, String periodKey);
    List<UserMissionEntity> findByUserIdAndPeriodKey(UUID userId, String periodKey);
}
