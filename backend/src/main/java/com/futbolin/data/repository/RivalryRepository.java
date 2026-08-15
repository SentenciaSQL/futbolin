package com.futbolin.data.repository;

import com.futbolin.data.entity.RivalryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RivalryRepository extends JpaRepository<RivalryEntity, UUID> {
    Optional<RivalryEntity> findByUserAIdAndUserBId(UUID userAId, UUID userBId);
    List<RivalryEntity> findByUserAIdOrUserBIdOrderByMatchesPlayedDesc(UUID a, UUID b);
}
