package com.futbolin.application.progression;

import com.futbolin.data.entity.AchievementEntity;
import com.futbolin.data.entity.MissionEntity;
import com.futbolin.data.entity.UserAchievementEntity;
import com.futbolin.data.entity.UserEntity;
import com.futbolin.data.entity.UserMissionEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.AchievementRepository;
import com.futbolin.data.repository.MissionRepository;
import com.futbolin.data.repository.UserAchievementRepository;
import com.futbolin.data.repository.UserMissionRepository;
import com.futbolin.domain.progression.ProgressionCalculator;
import com.futbolin.domain.ranking.Division;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProgressionService {

    private final ProgressionCalculator calculator = new ProgressionCalculator();
    private final AchievementRepository achievements;
    private final UserAchievementRepository userAchievements;
    private final MissionRepository missions;
    private final UserMissionRepository userMissions;

    public ProgressionService(
            AchievementRepository achievements,
            UserAchievementRepository userAchievements,
            MissionRepository missions,
            UserMissionRepository userMissions
    ) {
        this.achievements = achievements;
        this.userAchievements = userAchievements;
        this.missions = missions;
        this.userMissions = userMissions;
    }

    @Transactional
    public void applyMatchRewards(UserProfileEntity profile, boolean win, boolean draw, int goals, int correct, int streak, int xp, int coins) {
        profile.setXp(profile.getXp() + xp);
        profile.setCoins(profile.getCoins() + coins);
        profile.setLevel(calculator.levelForXp(profile.getXp()));
        profile.setMatchesPlayed(profile.getMatchesPlayed() + 1);
        if (win) {
            profile.setWins(profile.getWins() + 1);
        } else if (draw) {
            profile.setDraws(profile.getDraws() + 1);
        } else {
            profile.setLosses(profile.getLosses() + 1);
        }
        profile.setGoalsScored(profile.getGoalsScored() + goals);
        incrementMission(profile.getUser(), "PLAY_MATCH", 1);
        if (win) {
            incrementMission(profile.getUser(), "WIN_MATCH", 1);
        }
        incrementMission(profile.getUser(), "CORRECT_ANSWERS", correct);
        incrementMission(profile.getUser(), "SCORE_GOALS", goals);
        if (streak >= 5) {
            incrementMission(profile.getUser(), "ANSWER_STREAK", streak);
        }
        unlock(profile.getUser(), profile);
    }

    public int computeXp(boolean win, boolean draw, int goals, int correct, int streak) {
        return calculator.matchXp(win, draw, goals, correct, streak);
    }

    public int computeCoins(boolean win, int goals, int correct) {
        return calculator.matchCoins(win, goals, correct);
    }

    @Transactional
    public void incrementMission(UserEntity user, String metric, int amount) {
        if (amount <= 0) {
            return;
        }
        String dailyKey = LocalDate.now().toString();
        String weeklyKey = LocalDate.now().getYear() + "-W" + LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        for (MissionEntity mission : missions.findByActiveTrue()) {
            if (!mission.getMetric().equals(metric)) {
                continue;
            }
            String periodKey = "WEEKLY".equals(mission.getPeriod()) ? weeklyKey : dailyKey;
            UserMissionEntity progress = userMissions
                    .findByUserIdAndMissionIdAndPeriodKey(user.getId(), mission.getId(), periodKey)
                    .orElseGet(() -> {
                        UserMissionEntity created = new UserMissionEntity();
                        created.setUser(user);
                        created.setMission(mission);
                        created.setPeriodKey(periodKey);
                        return created;
                    });
            if (progress.isCompleted()) {
                continue;
            }
            progress.setProgress(Math.min(mission.getTarget(), progress.getProgress() + amount));
            progress.setUpdatedAt(Instant.now());
            if (progress.getProgress() >= mission.getTarget()) {
                progress.setCompleted(true);
            }
            userMissions.save(progress);
        }
    }

    @Transactional
    public void claimMission(UUID userId, UUID missionId, String periodKey, UserProfileEntity profile) {
        UserMissionEntity progress = userMissions.findByUserIdAndMissionIdAndPeriodKey(userId, missionId, periodKey)
                .orElseThrow();
        if (!progress.isCompleted() || progress.isClaimed()) {
            return;
        }
        progress.setClaimed(true);
        profile.setXp(profile.getXp() + progress.getMission().getXpReward());
        profile.setCoins(profile.getCoins() + progress.getMission().getCoinsReward());
        profile.setLevel(calculator.levelForXp(profile.getXp()));
    }

    private void unlock(UserEntity user, UserProfileEntity profile) {
        maybe(user, "FIRST_GOAL", profile.getGoalsScored() >= 1);
        maybe(user, "HAT_TRICK", profile.getGoalsScored() >= 3 && true);
        maybe(user, "ENCYCLOPEDIA", profile.getCorrectAnswers() >= 1000);
        maybe(user, "WORLD_CHAMPION", profile.getDivision() == Division.LEGEND);
        maybe(user, "UNSTOPPABLE", false);
    }

    public void unlockCode(UserEntity user, String code) {
        maybe(user, code, true);
    }

    private void maybe(UserEntity user, String code, boolean condition) {
        if (!condition) {
            return;
        }
        AchievementEntity achievement = achievements.findByCode(code).orElse(null);
        if (achievement == null) {
            return;
        }
        if (userAchievements.existsByUserIdAndAchievementId(user.getId(), achievement.getId())) {
            return;
        }
        UserAchievementEntity ua = new UserAchievementEntity();
        ua.setUser(user);
        ua.setAchievement(achievement);
        userAchievements.save(ua);
        if (user.getProfile() != null) {
            user.getProfile().setXp(user.getProfile().getXp() + achievement.getXpReward());
            user.getProfile().setCoins(user.getProfile().getCoins() + achievement.getCoinsReward());
        }
    }

    public List<MissionEntity> activeMissions() {
        return missions.findByActiveTrue();
    }
}
