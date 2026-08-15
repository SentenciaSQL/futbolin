package com.futbolin.data.repository;

import com.futbolin.data.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndTypeAndCreatedAtAfter(UUID userId, String type, java.time.Instant after);
}
