package com.futbolin.api.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futbolin.application.match.MatchService;
import com.futbolin.application.match.MatchmakingService;
import com.futbolin.application.presence.PresenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.UUID;

@Component
public class MatchWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MatchWebSocketHandler.class);

    private final WebSocketEventPublisher publisher;
    private final MatchService matchService;
    private final MatchmakingService matchmakingService;
    private final PresenceStore presenceStore;
    private final ObjectMapper objectMapper;

    public MatchWebSocketHandler(
            WebSocketEventPublisher publisher,
            MatchService matchService,
            MatchmakingService matchmakingService,
            PresenceStore presenceStore,
            ObjectMapper objectMapper
    ) {
        this.publisher = publisher;
        this.matchService = matchService;
        this.matchmakingService = matchmakingService;
        this.presenceStore = presenceStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID userId = userId(session);
        publisher.register(userId, session);
        presenceStore.heartbeat(userId);
        matchService.markReconnected(userId);
        log.debug("WS connected {}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UUID userId = userId(session);
        presenceStore.heartbeat(userId);
        JsonNode node = objectMapper.readTree(message.getPayload());
        String type = node.path("type").asText();
        switch (type) {
            case "QUEUE" -> {
                matchmakingService.enqueue(userId, node.path("latencyMs").asInt(50));
                matchmakingService.tryMatch(userId).ifPresent(pair -> matchService.createRanked(pair.a(), pair.b()));
                publisher.sendToUser(userId, Map.of("type", "QUEUED"));
            }
            case "CANCEL_QUEUE" -> matchmakingService.cancel(userId);
            case "ANSWER" -> matchService.submitAnswer(
                    userId,
                    UUID.fromString(node.get("matchId").asText()),
                    UUID.fromString(node.get("roundId").asText()),
                    node.path("optionKey").asText()
            );
            case "REMATCH" -> matchService.requestRematch(userId, UUID.fromString(node.get("matchId").asText()));
            case "EMOJI" -> {
                UUID matchId = UUID.fromString(node.get("matchId").asText());
                UUID opponent = opponentOf(matchId, userId);
                Map<String, Object> emoji = Map.of(
                        "type", "EMOJI",
                        "userId", userId,
                        "code", node.path("code").asText(),
                        "text", node.path("text").asText("")
                );
                publisher.sendToUser(userId, emoji);
                if (opponent != null && !matchService.isMutedBy(matchId, opponent)) {
                    publisher.sendToUser(opponent, emoji);
                }
            }
            case "MUTE" -> {
                UUID matchId = UUID.fromString(node.get("matchId").asText());
                matchService.muteOpponent(userId, matchId);
                publisher.sendToUser(userId, Map.of("type", "MUTED", "opponent", true, "matchId", matchId));
            }
            case "HEARTBEAT" -> presenceStore.heartbeat(userId);
            default -> publisher.sendToUser(userId, Map.of("type", "ERROR", "message", "Unknown event"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID userId = userId(session);
        publisher.unregister(userId, session);
        presenceStore.offline(userId);
        matchmakingService.cancel(userId);
        matchService.markDisconnected(userId);
    }

    private UUID opponentOf(UUID matchId, UUID userId) {
        return matchService.opponentId(matchId, userId);
    }

    private UUID userId(WebSocketSession session) {
        return (UUID) session.getAttributes().get("userId");
    }
}
