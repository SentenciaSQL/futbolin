package com.futbolin.api.v1.ranking;

import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.repository.RankingRepository;
import com.futbolin.data.repository.RankingSeasonRepository;
import com.futbolin.data.repository.UserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final UserProfileRepository profiles;
    private final RankingRepository rankings;
    private final RankingSeasonRepository seasons;

    public RankingController(UserProfileRepository profiles, RankingRepository rankings, RankingSeasonRepository seasons) {
        this.profiles = profiles;
        this.rankings = rankings;
        this.seasons = seasons;
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
}
