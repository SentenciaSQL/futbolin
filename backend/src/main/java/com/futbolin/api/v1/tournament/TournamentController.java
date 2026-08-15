package com.futbolin.api.v1.tournament;

import com.futbolin.application.tournament.TournamentService;
import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.entity.TournamentEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tournaments")
public class TournamentController {

    private final TournamentService tournaments;

    public TournamentController(TournamentService tournaments) {
        this.tournaments = tournaments;
    }

    @GetMapping
    public Object list() {
        return tournaments.list();
    }

    @PostMapping
    public TournamentEntity create(@RequestBody Map<String, String> body) {
        return tournaments.create(body.getOrDefault("name", "Copa Futbolín"), body.get("theme"));
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable UUID id) {
        return tournaments.detail(id);
    }

    @PostMapping("/{id}/join")
    public TournamentEntity join(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return tournaments.join(id, principal.id());
    }

    @PostMapping("/matches/{tournamentMatchId}/play")
    public Map<String, Object> play(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable UUID tournamentMatchId) {
        UUID matchId = tournaments.play(tournamentMatchId, principal.id());
        return Map.of("matchId", matchId);
    }
}
