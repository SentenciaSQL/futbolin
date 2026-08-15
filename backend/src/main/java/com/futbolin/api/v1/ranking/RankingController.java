package com.futbolin.api.v1.ranking;

import com.futbolin.application.social.FriendshipService;
import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.repository.MatchRepository;
import com.futbolin.data.repository.RankingRepository;
import com.futbolin.data.repository.RankingSeasonRepository;
import com.futbolin.data.repository.UserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final UserProfileRepository profiles;
    private final RankingRepository rankings;
    private final RankingSeasonRepository seasons;
    private final MatchRepository matches;
    private final FriendshipService friendships;

    public RankingController(
            UserProfileRepository profiles,
            RankingRepository rankings,
            RankingSeasonRepository seasons,
            MatchRepository matches,
            FriendshipService friendships
    ) {
        this.profiles = profiles;
        this.rankings = rankings;
        this.seasons = seasons;
        this.matches = matches;
        this.friendships = friendships;
    }

    @GetMapping
    public Object global(@RequestParam(defaultValue = "0") int page) {
        return profiles.findAllByOrderByRankingPointsDesc(PageRequest.of(page, 50));
    }

    @GetMapping("/country/{country}")
    public Object country(@PathVariable String country, @RequestParam(defaultValue = "0") int page) {
        return profiles.findByCountryOrderByRankingPointsDesc(country, PageRequest.of(page, 50));
    }

    @GetMapping("/season")
    public Object season(@RequestParam(defaultValue = "0") int page) {
        return seasons.findByActiveTrue()
                .map(s -> rankings.findBySeasonIdOrderByPointsDesc(s.getId(), PageRequest.of(page, 50)))
                .orElseGet(() -> rankings.findBySeasonIdOrderByPointsDesc(java.util.UUID.randomUUID(), PageRequest.of(page, 50)));
    }

    @GetMapping("/season/current")
    public Object currentSeason() {
        return seasons.findByActiveTrue().orElse(null);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("profile", profiles.findById(principal.id()).orElseThrow());
    }

    @GetMapping("/weekly")
    public List<Map<String, Object>> weekly() {
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Map<String, Object>> rows = new ArrayList<>();
        int rank = 1;
        for (Object[] row : matches.weeklyWinners(since)) {
            UUID userId = (UUID) row[0];
            long wins = ((Number) row[1]).longValue();
            var profile = profiles.findById(userId).orElse(null);
            if (profile == null) {
                continue;
            }
            rows.add(Map.of(
                    "rank", rank++,
                    "userId", userId,
                    "displayName", profile.getDisplayName() == null ? "" : profile.getDisplayName(),
                    "wins", wins,
                    "rankingPoints", profile.getRankingPoints(),
                    "division", profile.getDivision()
            ));
        }
        return rows;
    }

    @GetMapping("/friends")
    public Object friends(@AuthenticationPrincipal UserPrincipal principal) {
        return friendships.friends(principal.id());
    }
}
