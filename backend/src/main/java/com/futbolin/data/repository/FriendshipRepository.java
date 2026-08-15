package com.futbolin.data.repository;

import com.futbolin.data.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, UUID> {
    List<FriendshipEntity> findByRequesterIdOrAddresseeId(UUID requesterId, UUID addresseeId);
}
