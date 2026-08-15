package com.futbolin.api.v1;

import com.futbolin.application.daily.DailyChallengeService;
import com.futbolin.application.notification.NotificationService;
import com.futbolin.application.survival.SurvivalService;
import com.futbolin.core.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PlayModesController {

    private final DailyChallengeService dailyChallengeService;
    private final SurvivalService survivalService;
    private final NotificationService notifications;

    public PlayModesController(
            DailyChallengeService dailyChallengeService,
            SurvivalService survivalService,
            NotificationService notifications
    ) {
        this.dailyChallengeService = dailyChallengeService;
        this.survivalService = survivalService;
        this.notifications = notifications;
    }

    @GetMapping("/daily-challenge")
    public Object daily() {
        return dailyChallengeService.today();
    }

    @PostMapping("/daily-challenge/answer")
    public Map<String, Object> answerDaily(@AuthenticationPrincipal UserPrincipal principal,
                                           @RequestBody Map<String, String> body) {
        return dailyChallengeService.answer(principal.id(), body.get("optionKey"));
    }

    @PostMapping("/survival/start")
    public Map<String, Object> startSurvival(@AuthenticationPrincipal UserPrincipal principal) {
        return survivalService.start(principal.id());
    }

    @PostMapping("/survival/answer")
    public Map<String, Object> answerSurvival(@AuthenticationPrincipal UserPrincipal principal,
                                              @RequestBody Map<String, String> body) {
        return survivalService.answer(principal.id(), body.get("optionKey"));
    }

    @GetMapping("/survival/leaderboard")
    public Object survivalBoard() {
        return survivalService.leaderboard();
    }

    @GetMapping("/notifications")
    public Object notifications(@AuthenticationPrincipal UserPrincipal principal) {
        return notifications.list(principal.id());
    }
}
