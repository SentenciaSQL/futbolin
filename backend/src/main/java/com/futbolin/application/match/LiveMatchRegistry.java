package com.futbolin.application.match;

import com.futbolin.domain.match.LiveMatchState;
import com.futbolin.domain.match.MatchRules;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveMatchRegistry {

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> userToMatch = new ConcurrentHashMap<>();

    public Session create(LiveMatchState state, MatchRules rules) {
        Session session = new Session(state, rules);
        sessions.put(state.matchId(), session);
        userToMatch.put(state.playerA(), state.matchId());
        userToMatch.put(state.playerB(), state.matchId());
        return session;
    }

    public Optional<Session> get(UUID matchId) {
        return Optional.ofNullable(sessions.get(matchId));
    }

    public Optional<Session> byUser(UUID userId) {
        UUID matchId = userToMatch.get(userId);
        return matchId == null ? Optional.empty() : get(matchId);
    }

    public void remove(UUID matchId) {
        Session session = sessions.remove(matchId);
        if (session != null) {
            userToMatch.remove(session.state.playerA());
            userToMatch.remove(session.state.playerB());
        }
    }

    public Set<UUID> matchIds() {
        return Set.copyOf(sessions.keySet());
    }

    public static final class Session {
        public final LiveMatchState state;
        public final MatchRules rules;
        public Instant matchDeadline;
        public Instant currentRoundDeadline;
        public Instant disconnectDeadline;
        public UUID disconnectedUser;
        public UUID currentRoundId;
        public UUID currentQuestionId;
        public String shuffledKeys;
        public final Map<UUID, PendingAnswer> pending = new ConcurrentHashMap<>();
        public final Map<UUID, Boolean> rematch = new ConcurrentHashMap<>();
        public final Set<UUID> usedQuestions = ConcurrentHashMap.newKeySet();
        public int answerStreakA;
        public int answerStreakB;

        Session(LiveMatchState state, MatchRules rules) {
            this.state = state;
            this.rules = rules;
        }
    }

    public record PendingAnswer(String optionKey, Instant receivedAt, int responseMs, boolean correct) {}
}
