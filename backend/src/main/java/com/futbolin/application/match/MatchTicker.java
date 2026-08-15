package com.futbolin.application.match;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MatchTicker {

    private final MatchService matchService;
    private final MatchmakingService matchmakingService;

    public MatchTicker(MatchService matchService, MatchmakingService matchmakingService) {
        this.matchService = matchService;
        this.matchmakingService = matchmakingService;
    }

    @Scheduled(fixedDelay = 250)
    public void tickMatches() {
        matchService.tick();
    }

    @Scheduled(fixedDelay = 500)
    public void tickMatchmaking() {
        for (var ticket : matchmakingService.snapshot()) {
            matchmakingService.tryMatch(ticket.userId()).ifPresent(pair -> matchService.createRanked(pair.a(), pair.b()));
        }
    }
}
