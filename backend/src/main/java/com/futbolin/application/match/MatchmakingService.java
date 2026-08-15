package com.futbolin.application.match;

import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchmakingService {

    private final Map<UUID, MatchmakingTicket> queue = new ConcurrentHashMap<>();
    private final UserProfileRepository profiles;

    public MatchmakingService(UserProfileRepository profiles) {
        this.profiles = profiles;
    }

    public void enqueue(UUID userId, int latencyMs) {
        UserProfileEntity profile = profiles.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        queue.put(userId, MatchmakingTicket.from(profile, latencyMs));
    }

    public void cancel(UUID userId) {
        queue.remove(userId);
    }

    public boolean isQueued(UUID userId) {
        return queue.containsKey(userId);
    }

    public Optional<Pair> tryMatch(UUID userId) {
        MatchmakingTicket self = queue.get(userId);
        if (self == null) {
            return Optional.empty();
        }
        long waited = Duration.between(self.at().value(), Instant.now()).toMillis();
        int range = 80 + (int) (waited / 1000) * 60;
        List<MatchmakingTicket> others = new ArrayList<>(queue.values());
        return others.stream()
                .filter(t -> !t.userId().equals(userId))
                .filter(t -> Math.abs(t.rankingPoints() - self.rankingPoints()) <= range)
                .min(Comparator
                        .comparingInt((MatchmakingTicket t) -> countryBonus(self, t))
                        .thenComparingInt(t -> Math.abs(t.rankingPoints() - self.rankingPoints()))
                        .thenComparingInt(t -> Math.abs(t.latencyMs() - self.latencyMs())))
                .map(opp -> {
                    queue.remove(userId);
                    queue.remove(opp.userId());
                    return new Pair(userId, opp.userId());
                });
    }

    public List<MatchmakingTicket> snapshot() {
        return List.copyOf(queue.values());
    }

    private int countryBonus(MatchmakingTicket a, MatchmakingTicket b) {
        if (a.country() != null && a.country().equalsIgnoreCase(b.country())) {
            return 0;
        }
        return 1;
    }

    public record Pair(UUID a, UUID b) {}
}
