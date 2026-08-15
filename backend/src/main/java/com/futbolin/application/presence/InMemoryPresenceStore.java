package com.futbolin.application.presence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryPresenceStore implements PresenceStore {

    private static final long TTL_MS = 45_000;
    private final Map<UUID, Long> heartbeats = new ConcurrentHashMap<>();

    @Override
    public void heartbeat(UUID userId) {
        heartbeats.put(userId, Instant.now().toEpochMilli());
    }

    @Override
    public void offline(UUID userId) {
        heartbeats.remove(userId);
    }

    @Override
    public boolean isOnline(UUID userId) {
        Long ts = heartbeats.get(userId);
        return ts != null && Instant.now().toEpochMilli() - ts < TTL_MS;
    }

    @Override
    public Set<UUID> onlineAmong(Collection<UUID> ids) {
        return ids.stream().filter(this::isOnline).collect(Collectors.toSet());
    }
}
