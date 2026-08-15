package com.futbolin.api.v1.social;

import com.futbolin.application.presence.PresenceStore;
import com.futbolin.application.social.FriendshipService;
import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.entity.FriendshipEntity;
import com.futbolin.data.entity.UserProfileEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendshipService friendships;
    private final PresenceStore presence;

    public FriendController(FriendshipService friendships, PresenceStore presence) {
        this.friendships = friendships;
        this.presence = presence;
    }

    @PostMapping
    public FriendshipEntity request(@AuthenticationPrincipal UserPrincipal principal,
                                    @RequestBody Map<String, String> body) {
        if (body.get("username") != null && !body.get("username").isBlank()) {
            return friendships.requestByUsername(principal.id(), body.get("username"));
        }
        return friendships.request(principal.id(), UUID.fromString(body.get("userId")));
    }

    @PostMapping("/{id}/accept")
    public void accept(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        friendships.accept(principal.id(), id);
    }

    @GetMapping
    public List<Map<String, Object>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return friendships.friends(principal.id()).stream()
                .map(this::toRow)
                .toList();
    }

    @GetMapping("/pending")
    public Object pending(@AuthenticationPrincipal UserPrincipal principal) {
        return friendships.pending(principal.id());
    }

    @GetMapping("/ranking")
    public List<Map<String, Object>> ranking(@AuthenticationPrincipal UserPrincipal principal) {
        return list(principal);
    }

    private Map<String, Object> toRow(UserProfileEntity p) {
        return Map.of(
                "userId", p.getUserId(),
                "displayName", p.getDisplayName() == null ? "" : p.getDisplayName(),
                "rankingPoints", p.getRankingPoints(),
                "division", p.getDivision(),
                "online", presence.isOnline(p.getUserId())
        );
    }
}
