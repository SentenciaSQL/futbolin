package com.futbolin.data.repository;

import com.futbolin.data.entity.SurvivalRunEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SurvivalRunRepository extends JpaRepository<SurvivalRunEntity, UUID> {
    Page<SurvivalRunEntity> findAllByOrderByScoreDescCreatedAtAsc(Pageable pageable);
}
