package com.futbolin.data.repository;

import com.futbolin.data.entity.CosmeticEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CosmeticRepository extends JpaRepository<CosmeticEntity, UUID> {
    Optional<CosmeticEntity> findByKey(String key);
    List<CosmeticEntity> findByActiveTrue();
}
