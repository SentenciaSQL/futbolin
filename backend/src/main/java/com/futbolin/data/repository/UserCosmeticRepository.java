package com.futbolin.data.repository;

import com.futbolin.data.entity.UserCosmeticEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCosmeticRepository extends JpaRepository<UserCosmeticEntity, UUID> {
    List<UserCosmeticEntity> findByUserId(UUID userId);
    Optional<UserCosmeticEntity> findByUserIdAndCosmeticId(UUID userId, UUID cosmeticId);
    boolean existsByUserIdAndCosmeticId(UUID userId, UUID cosmeticId);
}
