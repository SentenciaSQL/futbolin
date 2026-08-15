package com.futbolin.data.repository;

import com.futbolin.data.entity.MissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MissionRepository extends JpaRepository<MissionEntity, UUID> {
    List<MissionEntity> findByActiveTrue();
    List<MissionEntity> findByActiveTrueAndPeriod(String period);
}
