package com.futbolin.api.v1.match;

import com.futbolin.application.match.MatchService;
import com.futbolin.application.match.MatchmakingService;
import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.entity.MatchEntity;
import com.futbolin.data.repository.MatchRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService matchService;
    private final MatchmakingService matchmakingService;
    private final MatchRepository matches;

    public MatchController(MatchService matchService, MatchmakingService matchmakingService, MatchRepository matches) {
        this.matchService = matchService;
        this.matchmakingService = matchmakingService;
        this.matches = matches;
    }

    @PostMapping("/queue")
    public Map<String, Object> queue(@AuthenticationPrincipal UserPrincipal principal,
                                     @RequestParam(defaultValue = "50") int latencyMs) {
        matchmakingService.enqueue(principal.id(), latencyMs);
        matchmakingService.tryMatch(principal.id()).ifPresent(pair -> matchService.createRanked(pair.a(), pair.b()));
        return Map.of("status", "QUEUED");
    }

    @DeleteMapping("/queue")
    public void cancel(@AuthenticationPrincipal UserPrincipal principal) {
        matchmakingService.cancel(principal.id());
    }

    @PostMapping("/private")
    public Map<String, Object> createPrivate(@AuthenticationPrincipal UserPrincipal principal) {
        MatchEntity match = matchService.createPrivate(principal.id());
        return Map.of(
                "matchId", match.getId(),
                "code", match.getPrivateCode(),
                "inviteUrl", "futbolin://join/" + match.getPrivateCode()
        );
    }

    @PostMapping("/private/{code}/join")
    public Map<String, Object> join(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String code) {
        MatchEntity match = matchService.joinPrivate(principal.id(), code);
        return Map.of("matchId", match.getId(), "status", match.getStatus().name());
    }

    @GetMapping("/history")
    public Object history(@AuthenticationPrincipal UserPrincipal principal, @RequestParam(defaultValue = "0") int page) {
        return matches.history(principal.id(), PageRequest.of(page, 20));
    }

    @GetMapping("/{id}")
    public MatchEntity get(@PathVariable UUID id) {
        return matches.findById(id).orElseThrow();
    }

    @PostMapping("/{id}/rematch")
    public void rematch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        matchService.requestRematch(principal.id(), id);
    }
}
