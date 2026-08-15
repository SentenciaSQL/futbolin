package com.futbolin.data.repository;

import com.futbolin.data.entity.MatchEntity;
import com.futbolin.domain.match.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {

    Optional<MatchEntity> findByPrivateCode(String privateCode);

    @Query("""
            SELECT m FROM MatchEntity m
            WHERE m.playerA.id = :userId OR m.playerB.id = :userId
            ORDER BY m.createdAt DESC
            """)
    Page<MatchEntity> history(@Param("userId") UUID userId, Pageable pageable);

    long countByStatus(MatchStatus status);
}
