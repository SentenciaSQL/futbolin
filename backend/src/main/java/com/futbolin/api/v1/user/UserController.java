package com.futbolin.api.v1.user;

import com.futbolin.api.dto.UpdateProfileRequest;
import com.futbolin.api.dto.UserProfileResponse;
import com.futbolin.application.user.UserService;
import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.repository.MatchRepository;
import com.futbolin.data.repository.RivalryRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final MatchRepository matches;
    private final RivalryRepository rivalries;

    public UserController(UserService userService, MatchRepository matches, RivalryRepository rivalries) {
        this.userService = userService;
        this.matches = matches;
        this.rivalries = rivalries;
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.me(principal.id());
    }

    @PatchMapping("/me")
    public UserProfileResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return userService.update(principal.id(), request);
    }

    @GetMapping("/{id}")
    public UserProfileResponse publicProfile(@PathVariable UUID id) {
        return userService.publicProfile(id);
    }

    @GetMapping("/me/stats")
    public Map<String, Object> stats(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.stats(principal.id());
    }

    @PostMapping("/me/daily-reward")
    public Map<String, Object> daily(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.claimDaily(principal.id());
    }

    @GetMapping("/me/rivalries")
    public Object rivalries(@AuthenticationPrincipal UserPrincipal principal) {
        return rivalries.findByUserAIdOrUserBIdOrderByMatchesPlayedDesc(principal.id(), principal.id());
    }

    @GetMapping("/me/history")
    public Object history(@AuthenticationPrincipal UserPrincipal principal,
                          @RequestParam(defaultValue = "0") int page) {
        return matches.history(principal.id(), PageRequest.of(page, 20));
    }
}
