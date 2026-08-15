package com.futbolin.application.match;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futbolin.application.progression.ProgressionService;
import com.futbolin.application.question.QuestionPicker;
import com.futbolin.application.ranking.RankingService;
import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.core.props.AppProperties;
import com.futbolin.core.util.Codes;
import com.futbolin.data.entity.*;
import com.futbolin.data.repository.*;
import com.futbolin.domain.match.*;
import com.futbolin.domain.question.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final MatchRepository matches;
    private final MatchPlayerRepository matchPlayers;
    private final MatchRoundRepository rounds;
    private final MatchAnswerRepository answers;
    private final MatchEventRepository events;
    private final MatchInvitationRepository invitations;
    private final UserRepository users;
    private final QuestionRepository questions;
    private final RivalryRepository rivalries;
    private final PlayerCategoryStatRepository categoryStats;
    private final LiveMatchRegistry registry;
    private final QuestionPicker picker;
    private final MatchEngine engine = new MatchEngine();
    private final RankingService rankingService;
    private final ProgressionService progressionService;
    private final MatchEventPublisher publisher;
    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    public MatchService(
            MatchRepository matches,
            MatchPlayerRepository matchPlayers,
            MatchRoundRepository rounds,
            MatchAnswerRepository answers,
            MatchEventRepository events,
            MatchInvitationRepository invitations,
            UserRepository users,
            QuestionRepository questions,
            RivalryRepository rivalries,
            PlayerCategoryStatRepository categoryStats,
            LiveMatchRegistry registry,
            QuestionPicker picker,
            RankingService rankingService,
            ProgressionService progressionService,
            MatchEventPublisher publisher,
            AppProperties properties,
            ObjectMapper objectMapper
    ) {
        this.matches = matches;
        this.matchPlayers = matchPlayers;
        this.rounds = rounds;
        this.answers = answers;
        this.events = events;
        this.invitations = invitations;
        this.users = users;
        this.questions = questions;
        this.rivalries = rivalries;
        this.categoryStats = categoryStats;
        this.registry = registry;
        this.picker = picker;
        this.rankingService = rankingService;
        this.progressionService = progressionService;
        this.publisher = publisher;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public MatchRules rules() {
        var m = properties.match();
        return new MatchRules(m.questionSeconds(), m.durationSeconds(), m.goalsToWin(),
                m.reconnectSeconds(), m.minAnswerMillis(), m.penaltyKicks());
    }

    @Transactional
    public MatchEntity createRanked(UUID userA, UUID userB) {
        return startLive(MatchMode.RANKED, userA, userB, null);
    }

    @Transactional
    public MatchEntity createPrivate(UUID hostId) {
        UserEntity host = users.findById(hostId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        MatchEntity match = new MatchEntity();
        match.setMode(MatchMode.PRIVATE);
        match.setStatus(MatchStatus.WAITING);
        match.setPrivateCode(Codes.privateMatchCode());
        match.setPlayerA(host);
        match.setDurationSeconds(rules().durationSeconds());
        match.setGoalsToWin(rules().goalsToWin());
        match.setPitchPhase(PitchPhase.KICKOFF);
        match.setSeason(rankingService.requireActive());
        matches.save(match);
        attachPlayer(match, host, "A");
        MatchInvitationEntity invitation = new MatchInvitationEntity();
        invitation.setMatch(match);
        invitation.setHost(host);
        invitation.setCode(match.getPrivateCode());
        invitation.setExpiresAt(Instant.now().plusSeconds(1800));
        invitations.save(invitation);
        return match;
    }

    @Transactional
    public MatchEntity joinPrivate(UUID guestId, String code) {
        MatchEntity match = matches.findByPrivateCode(code.toUpperCase())
                .orElseThrow(() -> new ApiException(ErrorCode.MATCH_NOT_FOUND));
        if (match.getPlayerB() != null) {
            throw new ApiException(ErrorCode.MATCH_FULL);
        }
        if (match.getStatus() != MatchStatus.WAITING) {
            throw new ApiException(ErrorCode.MATCH_ALREADY_STARTED);
        }
        UserEntity guest = users.findById(guestId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        match.setPlayerB(guest);
        attachPlayer(match, guest, "B");
        return startLive(match);
    }

    @Transactional
    public MatchEntity startLive(MatchMode mode, UUID userA, UUID userB, String code) {
        UserEntity a = users.findById(userA).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        UserEntity b = users.findById(userB).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        MatchEntity match = new MatchEntity();
        match.setMode(mode);
        match.setPrivateCode(code);
        match.setPlayerA(a);
        match.setPlayerB(b);
        match.setDurationSeconds(rules().durationSeconds());
        match.setGoalsToWin(rules().goalsToWin());
        match.setSeason(rankingService.requireActive());
        matches.save(match);
        attachPlayer(match, a, "A");
        attachPlayer(match, b, "B");
        return startLive(match);
    }

    private MatchEntity startLive(MatchEntity match) {
        match.setStatus(MatchStatus.LIVE);
        match.setStartedAt(Instant.now());
        match.setPitchPhase(PitchPhase.KICKOFF);
        match.setBallPosition(0);
        match.setPossessionUserId(match.getPlayerA().getId());
        LiveMatchState state = new LiveMatchState(
                match.getId(), match.getPlayerA().getId(), match.getPlayerB().getId(), rules()
        );
        LiveMatchRegistry.Session session = registry.create(state, rules());
        session.matchDeadline = Instant.now().plusSeconds(rules().durationSeconds());
        emit(match.getId(), "MATCH_STARTED", Map.of(
                "matchId", match.getId(),
                "playerA", publicPlayer(match.getPlayerA()),
                "playerB", publicPlayer(match.getPlayerB()),
                "durationSeconds", rules().durationSeconds(),
                "goalsToWin", rules().goalsToWin()
        ));
        publisher.sendToMatch(match.getId(), match.getPlayerA().getId(), match.getPlayerB().getId(), Map.of(
                "type", "MATCH_FOUND",
                "matchId", match.getId(),
                "playerA", publicPlayer(match.getPlayerA()),
                "playerB", publicPlayer(match.getPlayerB())
        ));
        openRound(match, session);
        return match;
    }

    private void attachPlayer(MatchEntity match, UserEntity user, String slot) {
        MatchPlayerEntity mp = new MatchPlayerEntity();
        mp.setMatch(match);
        mp.setUser(user);
        mp.setSlot(slot);
        mp.setRatingBefore(user.getProfile().getRankingPoints());
        matchPlayers.save(mp);
    }

    @Transactional
    public void submitAnswer(UUID userId, UUID matchId, UUID roundId, String optionKey) {
        LiveMatchRegistry.Session session = registry.get(matchId)
                .orElseThrow(() -> new ApiException(ErrorCode.MATCH_NOT_FOUND));
        if (!roundId.equals(session.currentRoundId)) {
            throw new ApiException(ErrorCode.NOT_YOUR_TURN, "Round is closed");
        }
        if (session.pending.containsKey(userId)) {
            throw new ApiException(ErrorCode.ALREADY_ANSWERED);
        }
        Instant now = Instant.now();
        MatchRoundEntity round = rounds.findById(roundId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        int responseMs = (int) Duration.between(round.getStartedAt(), now).toMillis();
        if (responseMs < rules().minAnswerMillis()) {
            throw new ApiException(ErrorCode.ANSWER_TOO_FAST);
        }
        QuestionEntity question = questions.findWithOptions(round.getQuestion().getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        boolean correct = question.getOptions().stream()
                .anyMatch(o -> o.getOptionKey().equals(optionKey) && o.isCorrect());
        session.pending.put(userId, new LiveMatchRegistry.PendingAnswer(optionKey, now, responseMs, correct));
        publisher.sendToMatch(matchId, session.state.playerA(), session.state.playerB(), Map.of(
                "type", "ANSWER_SUBMITTED",
                "matchId", matchId,
                "userId", userId
        ));
        if (session.pending.size() >= 2 || (session.state.phase() == PitchPhase.PENALTIES
                || session.state.phase() == PitchPhase.SUDDEN_DEATH) && session.pending.containsKey(penaltyTaker(session))) {
            closeRound(matchId, false);
        }
    }

    @Transactional
    public void closeRound(UUID matchId, boolean timeout) {
        LiveMatchRegistry.Session session = registry.get(matchId).orElse(null);
        if (session == null || session.currentRoundId == null) {
            return;
        }
        MatchEntity match = matches.findById(matchId).orElse(null);
        if (match == null) {
            return;
        }
        MatchRoundEntity round = rounds.findById(session.currentRoundId).orElse(null);
        if (round == null || round.getClosedAt() != null) {
            return;
        }
        QuestionEntity question = questions.findWithOptions(round.getQuestion().getId()).orElseThrow();
        List<SubmittedAnswer> submitted = new ArrayList<>();
        for (UUID player : List.of(session.state.playerA(), session.state.playerB())) {
            LiveMatchRegistry.PendingAnswer pending = session.pending.get(player);
            if (pending == null) {
                continue;
            }
            MatchAnswerEntity answer = new MatchAnswerEntity();
            answer.setRound(round);
            answer.setUser(users.getReferenceById(player));
            answer.setOptionKey(pending.optionKey());
            answer.setCorrect(pending.correct());
            answer.setReceivedAt(pending.receivedAt());
            answer.setResponseMs(pending.responseMs());
            answers.save(answer);
            submitted.add(new SubmittedAnswer(player, pending.optionKey(), pending.correct(), pending.responseMs()));
            if (pending.correct()) {
                question.setTimesCorrect(question.getTimesCorrect() + 1);
            }
            trackCategory(player, question.getCategory().getId(), pending.correct());
        }
        boolean timeExpired = timeout || Instant.now().isAfter(session.matchDeadline);
        RoundResolution resolution = engine.resolve(session.state, submitted, timeExpired);
        session.state.applyRound(resolution);
        round.setClosedAt(Instant.now());
        round.setWinnerUserId(resolution.roundWinnerId());
        persistState(match, session.state);
        for (String event : resolution.events()) {
            emit(matchId, event, snapshotPayload(session, resolution, question));
        }
        publisher.sendToMatch(matchId, session.state.playerA(), session.state.playerB(),
                snapshotPayload(session, resolution, question));
        session.pending.clear();
        session.currentRoundId = null;
        updateStreaks(session, submitted);
        if (session.state.isFinished()) {
            finish(match, session);
            return;
        }
        openRound(match, session);
    }

    private void openRound(MatchEntity match, LiveMatchRegistry.Session session) {
        session.state.nextRound();
        UserProfileEntity profileA = match.getPlayerA().getProfile();
        Difficulty difficulty = picker.adaptive(profileA.getRankingPoints(), profileA.accuracy(), session.answerStreakA);
        QuestionPicker.PickedQuestion picked = picker.pick(difficulty, null, List.copyOf(session.usedQuestions));
        session.usedQuestions.add(picked.question().getId());
        MatchRoundEntity round = new MatchRoundEntity();
        round.setMatch(match);
        round.setQuestion(picked.question());
        round.setRoundNumber(session.state.roundNumber());
        round.setPhase(session.state.phase());
        round.setStartedAt(Instant.now());
        round.setShuffledKeys(picked.shuffledKeys());
        rounds.save(round);
        session.currentRoundId = round.getId();
        session.currentQuestionId = picked.question().getId();
        session.shuffledKeys = picked.shuffledKeys();
        session.currentRoundDeadline = Instant.now().plusSeconds(rules().questionSeconds());
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "QUESTION");
        payload.put("matchId", match.getId());
        payload.put("roundId", round.getId());
        payload.put("seconds", rules().questionSeconds());
        payload.put("phase", session.state.phase().name());
        payload.put("ballPosition", session.state.ballPosition());
        payload.put("zone", session.state.zone().name());
        payload.put("possessionUserId", session.state.possessionUserId());
        payload.put("scoreA", session.state.scoreA());
        payload.put("scoreB", session.state.scoreB());
        payload.put("question", questionPayload(picked, "es"));
        publisher.sendToMatch(match.getId(), session.state.playerA(), session.state.playerB(), payload);
        emit(match.getId(), "QUESTION", Map.of("roundId", round.getId(), "questionId", picked.question().getId()));
    }

    @Transactional
    public void markDisconnected(UUID userId) {
        registry.byUser(userId).ifPresent(session -> {
            session.disconnectedUser = userId;
            session.disconnectDeadline = Instant.now().plusSeconds(rules().reconnectSeconds());
            MatchEntity match = matches.findById(session.state.matchId()).orElse(null);
            if (match != null) {
                match.setStatus(MatchStatus.RECONNECTING);
            }
            publisher.sendToMatch(session.state.matchId(), session.state.playerA(), session.state.playerB(), Map.of(
                    "type", "PLAYER_DISCONNECTED",
                    "userId", userId,
                    "reconnectSeconds", rules().reconnectSeconds()
            ));
        });
    }

    @Transactional
    public void markReconnected(UUID userId) {
        registry.byUser(userId).ifPresent(session -> {
            if (userId.equals(session.disconnectedUser)) {
                session.disconnectedUser = null;
                session.disconnectDeadline = null;
                matches.findById(session.state.matchId()).ifPresent(m -> m.setStatus(MatchStatus.LIVE));
                publisher.sendToMatch(session.state.matchId(), session.state.playerA(), session.state.playerB(), Map.of(
                        "type", "PLAYER_RECONNECTED",
                        "userId", userId,
                        "snapshot", snapshotPayload(session, null, null)
                ));
            }
        });
    }

    @Transactional
    public void tick() {
        Instant now = Instant.now();
        for (UUID matchId : registry.matchIds()) {
            registry.get(matchId).ifPresent(session -> {
                if (session.disconnectedUser != null && session.disconnectDeadline != null && now.isAfter(session.disconnectDeadline)) {
                    RoundResolution resolution = engine.abandon(session.state, session.disconnectedUser);
                    session.state.applyRound(resolution);
                    matches.findById(matchId).ifPresent(match -> {
                        persistState(match, session.state);
                        finish(match, session);
                    });
                    return;
                }
                if (session.currentRoundDeadline != null && now.isAfter(session.currentRoundDeadline) && session.currentRoundId != null) {
                    closeRound(matchId, Instant.now().isAfter(session.matchDeadline));
                }
            });
        }
    }

    @Transactional
    public void requestRematch(UUID userId, UUID matchId) {
        LiveMatchRegistry.Session session = registry.get(matchId).orElse(null);
        MatchEntity previous = matches.findById(matchId).orElseThrow(() -> new ApiException(ErrorCode.MATCH_NOT_FOUND));
        publisher.sendToMatch(matchId, previous.getPlayerA().getId(), previous.getPlayerB().getId(), Map.of(
                "type", "REMATCH_REQUESTED",
                "userId", userId
        ));
        if (session != null) {
            session.rematch.put(userId, true);
            if (Boolean.TRUE.equals(session.rematch.get(session.state.playerA()))
                    && Boolean.TRUE.equals(session.rematch.get(session.state.playerB()))) {
                registry.remove(matchId);
                createRanked(session.state.playerA(), session.state.playerB());
            }
        } else if (previous.getPlayerA() != null && previous.getPlayerB() != null) {
            UUID other = previous.getPlayerA().getId().equals(userId)
                    ? previous.getPlayerB().getId() : previous.getPlayerA().getId();
            createRanked(userId, other);
        }
    }

    private void finish(MatchEntity match, LiveMatchRegistry.Session session) {
        match.setStatus(MatchStatus.FINISHED);
        match.setEndedAt(Instant.now());
        match.setEndReason(session.state.endReason());
        if (session.state.winnerId() != null) {
            match.setWinner(users.getReferenceById(session.state.winnerId()));
        }
        persistState(match, session.state);
        MatchPlayerEntity mpA = matchPlayers.findByMatchIdAndUserId(match.getId(), session.state.playerA()).orElseThrow();
        MatchPlayerEntity mpB = matchPlayers.findByMatchIdAndUserId(match.getId(), session.state.playerB()).orElseThrow();
        int correctA = (int) answersOf(match.getId(), session.state.playerA());
        int correctB = (int) answersOf(match.getId(), session.state.playerB());
        int totalA = mpA.getCorrectAnswers() + mpA.getWrongAnswers();
        int totalB = mpB.getCorrectAnswers() + mpB.getWrongAnswers();
        mpA.setGoals(session.state.scoreA());
        mpB.setGoals(session.state.scoreB());
        UserProfileEntity pA = match.getPlayerA().getProfile();
        UserProfileEntity pB = match.getPlayerB().getProfile();
        boolean winA = session.state.winnerId() != null && session.state.winnerId().equals(session.state.playerA());
        boolean winB = session.state.winnerId() != null && session.state.winnerId().equals(session.state.playerB());
        boolean draw = session.state.winnerId() == null;
        var elo = rankingService.apply(pA, pB, session.state.scoreA(), session.state.scoreB(),
                correctA, Math.max(totalA, 1), session.state.scoreA(),
                correctB, Math.max(totalB, 1), session.state.scoreB());
        mpA.setRatingAfter(pA.getRankingPoints());
        mpB.setRatingAfter(pB.getRankingPoints());
        mpA.setRatingDelta(elo.deltaA());
        mpB.setRatingDelta(elo.deltaB());
        int xpA = progressionService.computeXp(winA, draw, session.state.scoreA(), correctA, session.answerStreakA);
        int xpB = progressionService.computeXp(winB, draw, session.state.scoreB(), correctB, session.answerStreakB);
        int coinsA = progressionService.computeCoins(winA, session.state.scoreA(), correctA);
        int coinsB = progressionService.computeCoins(winB, session.state.scoreB(), correctB);
        mpA.setXpEarned(xpA);
        mpB.setXpEarned(xpB);
        mpA.setCoinsEarned(coinsA);
        mpB.setCoinsEarned(coinsB);
        pA.setCorrectAnswers(pA.getCorrectAnswers() + correctA);
        pB.setCorrectAnswers(pB.getCorrectAnswers() + correctB);
        pA.setTotalAnswers(pA.getTotalAnswers() + Math.max(totalA, correctA));
        pB.setTotalAnswers(pB.getTotalAnswers() + Math.max(totalB, correctB));
        pA.setGoalsConceded(pA.getGoalsConceded() + session.state.scoreB());
        pB.setGoalsConceded(pB.getGoalsConceded() + session.state.scoreA());
        progressionService.applyMatchRewards(pA, winA, draw, session.state.scoreA(), correctA, session.answerStreakA, xpA, coinsA);
        progressionService.applyMatchRewards(pB, winB, draw, session.state.scoreB(), correctB, session.answerStreakB, xpB, coinsB);
        if (session.state.scoreA() >= 3) {
            progressionService.unlockCode(match.getPlayerA(), "HAT_TRICK");
        }
        if (session.state.scoreB() >= 3) {
            progressionService.unlockCode(match.getPlayerB(), "HAT_TRICK");
        }
        updateRivalry(session.state.playerA(), session.state.playerB(), winA, winB, draw);
        publisher.sendToMatch(match.getId(), session.state.playerA(), session.state.playerB(), Map.of(
                "type", "MATCH_FINISHED",
                "matchId", match.getId(),
                "scoreA", session.state.scoreA(),
                "scoreB", session.state.scoreB(),
                "winnerId", session.state.winnerId() == null ? "" : session.state.winnerId().toString(),
                "endReason", session.state.endReason() == null ? "" : session.state.endReason().name(),
                "ratingDeltaA", elo.deltaA(),
                "ratingDeltaB", elo.deltaB(),
                "xpA", xpA,
                "xpB", xpB
        ));
        registry.remove(match.getId());
        log.info("Match {} finished {}-{} reason={}", match.getId(), session.state.scoreA(), session.state.scoreB(), session.state.endReason());
    }

    private long answersOf(UUID matchId, UUID userId) {
        return rounds.findByMatchIdOrderByRoundNumberAsc(matchId).stream()
                .map(r -> answers.findByRoundIdAndUserId(r.getId(), userId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .filter(MatchAnswerEntity::isCorrect)
                .count();
    }

    private void updateStreaks(LiveMatchRegistry.Session session, List<SubmittedAnswer> submitted) {
        for (SubmittedAnswer answer : submitted) {
            if (session.state.isPlayerA(answer.userId())) {
                session.answerStreakA = answer.correct() ? session.answerStreakA + 1 : 0;
            } else {
                session.answerStreakB = answer.correct() ? session.answerStreakB + 1 : 0;
            }
            matchPlayers.findByMatchIdAndUserId(session.state.matchId(), answer.userId()).ifPresent(mp -> {
                if (answer.correct()) {
                    mp.setCorrectAnswers(mp.getCorrectAnswers() + 1);
                } else {
                    mp.setWrongAnswers(mp.getWrongAnswers() + 1);
                }
            });
        }
    }

    private void updateRivalry(UUID a, UUID b, boolean winA, boolean winB, boolean draw) {
        UUID first = a.compareTo(b) < 0 ? a : b;
        UUID second = a.compareTo(b) < 0 ? b : a;
        RivalryEntity rivalry = rivalries.findByUserAIdAndUserBId(first, second).orElseGet(() -> {
            RivalryEntity r = new RivalryEntity();
            r.setUserAId(first);
            r.setUserBId(second);
            return r;
        });
        rivalry.setMatchesPlayed(rivalry.getMatchesPlayed() + 1);
        rivalry.setLastMatchAt(Instant.now());
        if (draw) {
            rivalry.setDraws(rivalry.getDraws() + 1);
        } else if ((winA && first.equals(a)) || (winB && first.equals(b))) {
            rivalry.setWinsA(rivalry.getWinsA() + 1);
        } else {
            rivalry.setWinsB(rivalry.getWinsB() + 1);
        }
        rivalries.save(rivalry);
    }

    private void trackCategory(UUID userId, UUID categoryId, boolean correct) {
        PlayerCategoryStatEntity stat = categoryStats.findByUserIdAndCategoryId(userId, categoryId).orElseGet(() -> {
            PlayerCategoryStatEntity s = new PlayerCategoryStatEntity();
            s.setUserId(userId);
            s.setCategoryId(categoryId);
            return s;
        });
        stat.setTotal(stat.getTotal() + 1);
        if (correct) {
            stat.setCorrect(stat.getCorrect() + 1);
        }
        categoryStats.save(stat);
    }

    private void persistState(MatchEntity match, LiveMatchState state) {
        match.setScoreA(state.scoreA());
        match.setScoreB(state.scoreB());
        match.setBallPosition(state.ballPosition());
        match.setPossessionUserId(state.possessionUserId());
        match.setPitchPhase(state.phase());
        match.setEndReason(state.endReason());
    }

    private UUID penaltyTaker(LiveMatchRegistry.Session session) {
        return session.state.penaltyIndex() % 2 == 0 ? session.state.playerA() : session.state.playerB();
    }

    private Map<String, Object> snapshotPayload(LiveMatchRegistry.Session session, RoundResolution resolution, QuestionEntity question) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ANSWER_RESULT");
        payload.put("matchId", session.state.matchId());
        payload.put("ballPosition", session.state.ballPosition());
        payload.put("zone", session.state.zone().name());
        payload.put("possessionUserId", session.state.possessionUserId());
        payload.put("phase", session.state.phase().name());
        payload.put("scoreA", session.state.scoreA());
        payload.put("scoreB", session.state.scoreB());
        payload.put("events", session.state.eventLog());
        if (resolution != null) {
            payload.put("goal", resolution.goalScored());
            payload.put("scorerId", resolution.scorerId());
            payload.put("roundWinnerId", resolution.roundWinnerId());
        }
        if (question != null) {
            payload.put("explanationEs", question.getExplanationEs());
            payload.put("explanationEn", question.getExplanationEn());
            payload.put("correctKey", question.getCorrectKey());
        }
        return payload;
    }

    private Map<String, Object> questionPayload(QuestionPicker.PickedQuestion picked, String lang) {
        boolean es = !"en".equals(lang);
        Map<String, Object> q = new HashMap<>();
        q.put("id", picked.question().getId());
        q.put("type", picked.question().getType().name());
        q.put("difficulty", picked.question().getDifficulty().name());
        q.put("prompt", es ? picked.question().getPromptEs() : picked.question().getPromptEn());
        q.put("imageUrl", picked.question().getImageUrl());
        q.put("metadata", picked.question().getMetadataJson());
        q.put("options", picked.shuffledOptions().stream().map(o -> Map.of(
                "key", o.getOptionKey(),
                "text", es ? o.getTextEs() : o.getTextEn()
        )).toList());
        return q;
    }

    private Map<String, Object> publicPlayer(UserEntity user) {
        UserProfileEntity p = user.getProfile();
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", p.getDisplayName(),
                "avatarKey", p.getAvatarKey(),
                "division", p.getDivision().name(),
                "level", p.getLevel(),
                "rankingPoints", p.getRankingPoints()
        );
    }

    private void emit(UUID matchId, String type, Map<String, Object> payload) {
        MatchEventEntity event = new MatchEventEntity();
        event.setMatchId(matchId);
        event.setEventType(type);
        try {
            event.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            event.setPayloadJson("{}");
        }
        events.save(event);
    }
}
