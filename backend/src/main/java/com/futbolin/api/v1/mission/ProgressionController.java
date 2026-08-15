package com.futbolin.api.v1.mission;

import com.futbolin.application.progression.ProgressionService;
import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.repository.AchievementRepository;
import com.futbolin.data.repository.UserAchievementRepository;
import com.futbolin.data.repository.UserMissionRepository;
import com.futbolin.data.repository.UserProfileRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ProgressionController {

    private final ProgressionService progressionService;
    private final UserMissionRepository userMissions;
    private final AchievementRepository achievements;
    private final UserAchievementRepository userAchievements;
    private final UserProfileRepository profiles;

    public ProgressionController(
            ProgressionService progressionService,
            UserMissionRepository userMissions,
            AchievementRepository achievements,
            UserAchievementRepository userAchievements,
            UserProfileRepository profiles
    ) {
        this.progressionService = progressionService;
        this.userMissions = userMissions;
        this.achievements = achievements;
        this.userAchievements = userAchievements;
        this.profiles = profiles;
    }

    @GetMapping("/missions")
    public Object missions(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of(
                "catalog", progressionService.activeMissions(),
                "progress", userMissions.findByUserIdAndPeriodKey(principal.id(), LocalDate.now().toString())
        );
    }

    @PostMapping("/missions/{id}/claim")
    public void claim(@AuthenticationPrincipal UserPrincipal principal,
                      @PathVariable UUID id,
                      @RequestParam(required = false) String periodKey) {
        String key = periodKey == null ? LocalDate.now().toString() : periodKey;
        progressionService.claimMission(principal.id(), id, key, profiles.findById(principal.id()).orElseThrow());
    }

    @GetMapping("/achievements")
    public Object achievements(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of(
                "catalog", achievements.findAll(),
                "unlocked", userAchievements.findByUserId(principal.id())
        );
    }
}
