package com.futbolin.data.repository;

import com.futbolin.data.entity.MatchEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchEventRepository extends JpaRepository<MatchEventEntity, UUID> {
    List<MatchEventEntity> findByMatchIdOrderByCreatedAtAsc(UUID matchId);
}
