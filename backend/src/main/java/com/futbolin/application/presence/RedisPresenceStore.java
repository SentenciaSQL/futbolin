package com.futbolin.application.presence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true")
public class RedisPresenceStore implements PresenceStore {

    private final StringRedisTemplate redis;

    public RedisPresenceStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void heartbeat(UUID userId) {
        redis.opsForValue().set("presence:" + userId, "1", Duration.ofSeconds(45));
    }

    @Override
    public void offline(UUID userId) {
        redis.delete("presence:" + userId);
    }

    @Override
    public boolean isOnline(UUID userId) {
        return Boolean.TRUE.equals(redis.hasKey("presence:" + userId));
    }

    @Override
    public Set<UUID> onlineAmong(Collection<UUID> ids) {
        Set<UUID> online = new HashSet<>();
        for (UUID id : ids) {
            if (isOnline(id)) {
                online.add(id);
            }
        }
        return online;
    }
}
