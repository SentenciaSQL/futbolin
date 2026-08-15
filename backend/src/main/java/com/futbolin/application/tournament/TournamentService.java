package com.futbolin.application.tournament;

import com.futbolin.application.match.MatchCompletedEvent;
import com.futbolin.application.match.MatchService;
import com.futbolin.application.notification.NotificationService;
import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.core.util.Codes;
import com.futbolin.data.entity.TournamentEntity;
import com.futbolin.data.entity.TournamentEntryEntity;
import com.futbolin.data.entity.TournamentMatchEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.TournamentEntryRepository;
import com.futbolin.data.repository.TournamentMatchRepository;
import com.futbolin.data.repository.TournamentRepository;
import com.futbolin.data.repository.UserProfileRepository;
import com.futbolin.domain.match.MatchMode;
import com.futbolin.domain.tournament.TournamentBracket;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TournamentService {

    private final TournamentRepository tournaments;
    private final TournamentEntryRepository entries;
    private final TournamentMatchRepository bracket;
    private final UserProfileRepository profiles;
    private final MatchService matchService;
    private final NotificationService notifications;

    public TournamentService(
            TournamentRepository tournaments,
            TournamentEntryRepository entries,
            TournamentMatchRepository bracket,
            UserProfileRepository profiles,
            MatchService matchService,
            NotificationService notifications
    ) {
        this.tournaments = tournaments;
        this.entries = entries;
        this.bracket = bracket;
        this.profiles = profiles;
        this.matchService = matchService;
        this.notifications = notifications;
    }

    @Transactional
    public TournamentEntity create(String name, String theme) {
        TournamentEntity t = new TournamentEntity();
        t.setName(name);
        t.setTheme(theme == null || theme.isBlank() ? "WEEKEND" : theme);
        t.setSlug(Codes.normalizeUsername(name.replace(' ', '-')) + "-" + Codes.random(4).toLowerCase());
        t.setStatus("REGISTRATION");
        t.setSize(TournamentBracket.SIZE);
        return tournaments.save(t);
    }

    @Transactional
    public TournamentEntity join(UUID tournamentId, UUID userId) {
        TournamentEntity t = tournaments.findById(tournamentId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!"REGISTRATION".equals(t.getStatus())) {
            throw new ApiException(ErrorCode.TOURNAMENT_NOT_JOINABLE);
        }
        if (entries.existsByTournamentIdAndUserId(tournamentId, userId)) {
            throw new ApiException(ErrorCode.ALREADY_JOINED);
        }
        if (entries.countByTournamentId(tournamentId) >= t.getSize()) {
            throw new ApiException(ErrorCode.TOURNAMENT_FULL);
        }
        TournamentEntryEntity entry = new TournamentEntryEntity();
        entry.setTournamentId(tournamentId);
        entry.setUserId(userId);
        entries.save(entry);
        if (entries.countByTournamentId(tournamentId) >= t.getSize()) {
            start(t);
        }
        return t;
    }

    private void start(TournamentEntity t) {
        List<TournamentEntryEntity> players = entries.findByTournamentIdOrderBySeedAsc(t.getId());
        players.sort(Comparator.comparingInt((TournamentEntryEntity e) ->
                profiles.findById(e.getUserId()).map(UserProfileEntity::getRankingPoints).orElse(1000)
        ).reversed());
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setSeed(i + 1);
        }
        Map<Integer, UUID> bySeed = new HashMap<>();
        players.forEach(p -> bySeed.put(p.getSeed(), p.getUserId()));
        for (TournamentBracket.Pairing pairing : TournamentBracket.roundOf16()) {
            TournamentMatchEntity match = new TournamentMatchEntity();
            match.setTournamentId(t.getId());
            match.setRoundName(TournamentBracket.Round.R16.name());
            match.setSlot(pairing.slot());
            match.setPlayerAId(bySeed.get(pairing.seedA()));
            match.setPlayerBId(bySeed.get(pairing.seedB()));
            match.setStatus("READY");
            bracket.save(match);
        }
        t.setStatus("IN_PROGRESS");
        t.setStartsAt(Instant.now());
        players.forEach(p -> notifications.notify(
                p.getUserId(),
                "TOURNAMENT",
                "El torneo comienza",
                "The tournament is starting",
                "Octavos listos. ¡A jugar!",
                "Round of 16 is ready. Play now!"
        ));
    }

    @Transactional
    public UUID play(UUID tournamentMatchId, UUID userId) {
        TournamentMatchEntity tm = bracket.findById(tournamentMatchId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userId.equals(tm.getPlayerAId()) && !userId.equals(tm.getPlayerBId())) {
            throw new ApiException(ErrorCode.NOT_YOUR_MATCH);
        }
        if (tm.getPlayerAId() == null || tm.getPlayerBId() == null) {
            throw new ApiException(ErrorCode.CONFLICT, "Waiting for opponent");
        }
        if (tm.getMatchId() != null) {
            return tm.getMatchId();
        }
        var live = matchService.startLive(MatchMode.TOURNAMENT, tm.getPlayerAId(), tm.getPlayerBId(), null);
        tm.setMatchId(live.getId());
        tm.setStatus("LIVE");
        return live.getId();
    }

    @EventListener
    @Transactional
    public void onMatchCompleted(MatchCompletedEvent event) {
        if (event.mode() != MatchMode.TOURNAMENT || event.winnerId() == null) {
            return;
        }
        TournamentMatchEntity current = bracket.findByMatchId(event.matchId()).orElse(null);
        if (current == null || current.getWinnerId() != null) {
            return;
        }
        current.setWinnerId(event.winnerId());
        current.setStatus("DONE");
        UUID loser = current.getPlayerAId().equals(event.winnerId()) ? current.getPlayerBId() : current.getPlayerAId();
        entries.findByTournamentIdAndUserId(current.getTournamentId(), loser).ifPresent(e -> e.setEliminated(true));
        TournamentBracket.Round round = TournamentBracket.Round.valueOf(current.getRoundName());
        TournamentBracket.Advancement next = TournamentBracket.next(round, current.getSlot());
        if (next == null) {
            TournamentEntity t = tournaments.findById(current.getTournamentId()).orElseThrow();
            t.setStatus("FINISHED");
            notifications.notify(event.winnerId(), "TOURNAMENT",
                    "¡Campeón del torneo!", "Tournament champion!",
                    "Ganaste " + t.getName(), "You won " + t.getName());
            return;
        }
        TournamentMatchEntity nextMatch = bracket
                .findByTournamentIdAndRoundNameAndSlot(current.getTournamentId(), next.nextRound().name(), next.nextSlot())
                .orElseGet(() -> {
                    TournamentMatchEntity created = new TournamentMatchEntity();
                    created.setTournamentId(current.getTournamentId());
                    created.setRoundName(next.nextRound().name());
                    created.setSlot(next.nextSlot());
                    created.setStatus("PENDING");
                    return created;
                });
        if (next.side() == 0) {
            nextMatch.setPlayerAId(event.winnerId());
        } else {
            nextMatch.setPlayerBId(event.winnerId());
        }
        if (nextMatch.getPlayerAId() != null && nextMatch.getPlayerBId() != null) {
            nextMatch.setStatus("READY");
        }
        bracket.save(nextMatch);
    }

    public List<TournamentEntity> list() {
        return tournaments.findAllByOrderByCreatedAtDesc();
    }

    public Map<String, Object> detail(UUID id) {
        TournamentEntity t = tournaments.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return Map.of(
                "tournament", t,
                "entries", entries.findByTournamentIdOrderBySeedAsc(id),
                "matches", bracket.findByTournamentIdOrderByRoundNameAscSlotAsc(id)
        );
    }
}
