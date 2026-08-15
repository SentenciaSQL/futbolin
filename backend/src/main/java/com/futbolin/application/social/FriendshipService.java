package com.futbolin.application.social;

import com.futbolin.application.notification.NotificationService;
import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.core.util.Codes;
import com.futbolin.data.entity.FriendshipEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.FriendshipRepository;
import com.futbolin.data.repository.UserProfileRepository;
import com.futbolin.data.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService {

    private final FriendshipRepository friendships;
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final NotificationService notifications;

    public FriendshipService(
            FriendshipRepository friendships,
            UserRepository users,
            UserProfileRepository profiles,
            NotificationService notifications
    ) {
        this.friendships = friendships;
        this.users = users;
        this.profiles = profiles;
        this.notifications = notifications;
    }

    @Transactional
    public FriendshipEntity requestByUsername(UUID from, String username) {
        var target = users.findByUsername(Codes.normalizeUsername(username))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return request(from, target.getId());
    }

    @Transactional
    public FriendshipEntity request(UUID from, UUID to) {
        if (from.equals(to)) {
            throw new ApiException(ErrorCode.VALIDATION, "Cannot friend yourself");
        }
        users.findById(to).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        FriendshipEntity existing = friendships.findByRequesterIdAndAddresseeId(from, to)
                .or(() -> friendships.findByRequesterIdAndAddresseeId(to, from))
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        FriendshipEntity f = new FriendshipEntity();
        f.setRequesterId(from);
        f.setAddresseeId(to);
        f.setStatus("PENDING");
        friendships.save(f);
        notifications.notify(to, "FRIEND",
                "Nueva solicitud de amistad", "New friend request",
                "Alguien quiere añadirte.", "Someone wants to add you.");
        return f;
    }

    @Transactional
    public void accept(UUID userId, UUID friendshipId) {
        FriendshipEntity f = friendships.findById(friendshipId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userId.equals(f.getAddresseeId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        f.setStatus("ACCEPTED");
        notifications.notify(f.getRequesterId(), "FRIEND",
                "Solicitud aceptada", "Request accepted",
                "Ya sois amigos.", "You are now friends.");
    }

    public List<UserProfileEntity> friends(UUID userId) {
        List<UserProfileEntity> result = new ArrayList<>();
        for (FriendshipEntity f : friendships.findByRequesterIdOrAddresseeId(userId, userId)) {
            if (!"ACCEPTED".equals(f.getStatus())) {
                continue;
            }
            UUID other = f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId();
            profiles.findById(other).ifPresent(result::add);
        }
        result.sort((a, b) -> Integer.compare(b.getRankingPoints(), a.getRankingPoints()));
        return result;
    }

    public List<FriendshipEntity> pending(UUID userId) {
        return friendships.findByRequesterIdOrAddresseeId(userId, userId).stream()
                .filter(f -> "PENDING".equals(f.getStatus()))
                .toList();
    }
}
