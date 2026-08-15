package com.futbolin.data.repository;

import com.futbolin.data.entity.PlayerCategoryStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerCategoryStatRepository extends JpaRepository<PlayerCategoryStatEntity, UUID> {
    List<PlayerCategoryStatEntity> findByUserId(UUID userId);
    Optional<PlayerCategoryStatEntity> findByUserIdAndCategoryId(UUID userId, UUID categoryId);
}
