package com.futbolin.data.repository;

import com.futbolin.data.entity.DailyLoginRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyLoginRewardRepository extends JpaRepository<DailyLoginRewardEntity, UUID> {
    List<DailyLoginRewardEntity> findAllByOrderByDayIndexAsc();
    Optional<DailyLoginRewardEntity> findByDayIndex(int dayIndex);
}
