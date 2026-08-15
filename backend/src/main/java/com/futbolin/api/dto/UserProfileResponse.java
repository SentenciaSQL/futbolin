package com.futbolin.api.dto;

import com.futbolin.data.entity.UserEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.domain.ranking.Division;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String username,
        String displayName,
        String avatarKey,
        String frameKey,
        String titleKey,
        String country,
        String favoriteTeam,
        int level,
        long xp,
        int coins,
        int matchesPlayed,
        int wins,
        int losses,
        int draws,
        int goalsScored,
        int goalsConceded,
        double accuracy,
        double winRate,
        int bestStreak,
        int dailyStreak,
        int rankingPoints,
        int peakRankingPoints,
        Division division,
        Integer averageAnswerMs,
        int survivalBest
) {
    public static UserProfileResponse from(UserEntity user, UserProfileEntity p) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                p.getDisplayName(),
                p.getAvatarKey(),
                p.getFrameKey(),
                p.getTitleKey(),
                p.getCountry(),
                p.getFavoriteTeam(),
                p.getLevel(),
                p.getXp(),
                p.getCoins(),
                p.getMatchesPlayed(),
                p.getWins(),
                p.getLosses(),
                p.getDraws(),
                p.getGoalsScored(),
                p.getGoalsConceded(),
                p.accuracy(),
                p.winRate(),
                p.getBestAnswerStreak(),
                p.getDailyStreak(),
                p.getRankingPoints(),
                p.getPeakRankingPoints(),
                p.getDivision(),
                p.getAverageAnswerMs(),
                p.getSurvivalBest()
        );
    }

    public static UserProfileResponse publicView(UserEntity user, UserProfileEntity p) {
        return new UserProfileResponse(
                user.getId(),
                null,
                user.getUsername(),
                p.getDisplayName(),
                p.getAvatarKey(),
                p.getFrameKey(),
                p.getTitleKey(),
                p.getCountry(),
                p.getFavoriteTeam(),
                p.getLevel(),
                p.getXp(),
                0,
                p.getMatchesPlayed(),
                p.getWins(),
                p.getLosses(),
                p.getDraws(),
                p.getGoalsScored(),
                p.getGoalsConceded(),
                p.accuracy(),
                p.winRate(),
                p.getBestAnswerStreak(),
                0,
                p.getRankingPoints(),
                p.getPeakRankingPoints(),
                p.getDivision(),
                p.getAverageAnswerMs(),
                p.getSurvivalBest()
        );
    }
}
