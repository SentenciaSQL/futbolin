package com.futbolin.application.match;

import java.util.Map;
import java.util.UUID;

public interface MatchEventPublisher {
    void sendToUser(UUID userId, Map<String, Object> payload);
    void sendToMatch(UUID matchId, UUID playerA, UUID playerB, Map<String, Object> payload);
}
