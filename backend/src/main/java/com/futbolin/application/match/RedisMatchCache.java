package com.futbolin.application.match;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true")
public class RedisMatchCache {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisMatchCache(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void putTicket(UUID userId, int rankingPoints) {
        redis.opsForZSet().add("matchmaking", userId.toString(), rankingPoints);
        redis.expire("matchmaking", Duration.ofMinutes(2));
    }

    public void removeTicket(UUID userId) {
        redis.opsForZSet().remove("matchmaking", userId.toString());
    }

    public void cacheLiveMatch(UUID matchId, String json) {
        redis.opsForValue().set("match:" + matchId, json, Duration.ofMinutes(15));
    }

    public String snapshot(UUID matchId) {
        return redis.opsForValue().get("match:" + matchId);
    }

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
