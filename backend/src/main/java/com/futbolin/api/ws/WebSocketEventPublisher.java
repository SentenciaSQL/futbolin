package com.futbolin.api.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futbolin.application.match.MatchEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventPublisher implements MatchEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventPublisher.class);

    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public WebSocketEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(UUID userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void unregister(UUID userId, WebSocketSession session) {
        sessions.remove(userId, session);
    }

    @Override
    public void sendToUser(UUID userId, Map<String, Object> payload) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        } catch (IOException e) {
            log.debug("WS send failed for {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendToMatch(UUID matchId, UUID playerA, UUID playerB, Map<String, Object> payload) {
        sendToUser(playerA, payload);
        sendToUser(playerB, payload);
    }
}
