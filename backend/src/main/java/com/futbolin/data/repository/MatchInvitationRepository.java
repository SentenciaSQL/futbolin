package com.futbolin.data.repository;

import com.futbolin.data.entity.MatchInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MatchInvitationRepository extends JpaRepository<MatchInvitationEntity, UUID> {
    Optional<MatchInvitationEntity> findByCode(String code);
}
