package com.futbolin.application.user;

import com.futbolin.api.dto.UpdateProfileRequest;
import com.futbolin.api.dto.UserProfileResponse;
import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.data.entity.DailyLoginRewardEntity;
import com.futbolin.data.entity.UserEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.DailyLoginRewardRepository;
import com.futbolin.data.repository.PlayerCategoryStatRepository;
import com.futbolin.data.repository.UserProfileRepository;
import com.futbolin.data.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final DailyLoginRewardRepository dailyRewards;
    private final PlayerCategoryStatRepository categoryStats;

    public UserService(
            UserRepository users,
            UserProfileRepository profiles,
            DailyLoginRewardRepository dailyRewards,
            PlayerCategoryStatRepository categoryStats
    ) {
        this.users = users;
        this.profiles = profiles;
        this.dailyRewards = dailyRewards;
        this.categoryStats = categoryStats;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse me(UUID userId) {
        UserEntity user = users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return UserProfileResponse.from(user, user.getProfile());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse publicProfile(UUID userId) {
        UserEntity user = users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return UserProfileResponse.publicView(user, user.getProfile());
    }

    @Transactional
    public UserProfileResponse update(UUID userId, UpdateProfileRequest request) {
        UserEntity user = users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        UserProfileEntity p = user.getProfile();
        if (request.displayName() != null && !request.displayName().isBlank()) {
            p.setDisplayName(request.displayName());
        }
        if (request.country() != null) {
            p.setCountry(request.country());
        }
        if (request.favoriteTeam() != null) {
            p.setFavoriteTeam(request.favoriteTeam());
        }
        if (request.avatarKey() != null) {
            p.setAvatarKey(request.avatarKey());
        }
        if (request.frameKey() != null) {
            p.setFrameKey(request.frameKey());
        }
        if (request.titleKey() != null) {
            p.setTitleKey(request.titleKey());
        }
        return UserProfileResponse.from(user, p);
    }

    @Transactional
    public Map<String, Object> claimDaily(UUID userId) {
        UserProfileEntity profile = profiles.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        LocalDate today = LocalDate.now();
        if (today.equals(profile.getLastDailyClaim())) {
            throw new ApiException(ErrorCode.CONFLICT, "Daily reward already claimed");
        }
        int streak = profile.getDailyStreak();
        if (profile.getLastDailyClaim() != null && profile.getLastDailyClaim().plusDays(1).equals(today)) {
            streak = streak + 1;
        } else {
            streak = 1;
        }
        int dayIndex = ((streak - 1) % 7) + 1;
        DailyLoginRewardEntity reward = dailyRewards.findByDayIndex(dayIndex)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        profile.setDailyStreak(streak);
        profile.setLastDailyClaim(today);
        profile.setCoins(profile.getCoins() + reward.getCoins());
        profile.setXp(profile.getXp() + reward.getXp());
        return Map.of(
                "day", dayIndex,
                "streak", streak,
                "coins", reward.getCoins(),
                "xp", reward.getXp(),
                "cosmeticKey", reward.getCosmeticKey() == null ? "" : reward.getCosmeticKey()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> stats(UUID userId) {
        UserEntity user = users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        UserProfileEntity p = user.getProfile();
        var cats = categoryStats.findByUserId(userId);
        String best = cats.stream()
                .filter(c -> c.getTotal() >= 5)
                .max((a, b) -> Double.compare(ratio(a.getCorrect(), a.getTotal()), ratio(b.getCorrect(), b.getTotal())))
                .map(c -> c.getCategoryId().toString())
                .orElse(null);
        String worst = cats.stream()
                .filter(c -> c.getTotal() >= 5)
                .min((a, b) -> Double.compare(ratio(a.getCorrect(), a.getTotal()), ratio(b.getCorrect(), b.getTotal())))
                .map(c -> c.getCategoryId().toString())
                .orElse(null);
        return Map.ofEntries(
                Map.entry("profile", UserProfileResponse.from(user, p)),
                Map.entry("bestCategoryId", best == null ? "" : best),
                Map.entry("worstCategoryId", worst == null ? "" : worst),
                Map.entry("categories", cats.stream().collect(Collectors.toList()))
        );
    }

    private double ratio(int a, int b) {
        return b == 0 ? 0 : (double) a / b;
    }
}
