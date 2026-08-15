package com.futbolin.data.repository;

import com.futbolin.data.entity.TournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentRepository extends JpaRepository<TournamentEntity, UUID> {
    Optional<TournamentEntity> findBySlug(String slug);
    List<TournamentEntity> findByStatusOrderByCreatedAtDesc(String status);
    List<TournamentEntity> findAllByOrderByCreatedAtDesc();
}
