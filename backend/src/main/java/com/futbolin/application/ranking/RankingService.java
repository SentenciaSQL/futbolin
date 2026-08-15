package com.futbolin.application.ranking;

import com.futbolin.data.entity.RankingEntity;
import com.futbolin.data.entity.RankingSeasonEntity;
import com.futbolin.data.entity.UserEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.RankingRepository;
import com.futbolin.data.repository.RankingSeasonRepository;
import com.futbolin.domain.ranking.Division;
import com.futbolin.domain.ranking.EloCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RankingService {

    private final EloCalculator elo = new EloCalculator();
    private final RankingRepository rankings;
    private final RankingSeasonRepository seasons;

    public RankingService(RankingRepository rankings, RankingSeasonRepository seasons) {
        this.rankings = rankings;
        this.seasons = seasons;
    }

    public EloCalculator.Result apply(
            UserProfileEntity a,
            UserProfileEntity b,
            int scoreA,
            int scoreB,
            int correctA,
            int totalA,
            int goalsA,
            int correctB,
            int totalB,
            int goalsB
    ) {
        double matchScore = elo.matchScore(scoreA, scoreB);
        double perfA = elo.performance(correctA, totalA, goalsA, Math.max(1, goalsA + goalsB));
        double perfB = elo.performance(correctB, totalB, goalsB, Math.max(1, goalsA + goalsB));
        EloCalculator.Result result = elo.calculate(
                a.getRankingPoints(), b.getRankingPoints(), matchScore,
                a.getMatchesPlayed(), b.getMatchesPlayed(), perfA, perfB
        );
        a.setRankingPoints(result.newA());
        b.setRankingPoints(result.newB());
        a.setDivision(Division.fromPoints(a.getRankingPoints()));
        b.setDivision(Division.fromPoints(b.getRankingPoints()));
        if (a.getRankingPoints() > a.getPeakRankingPoints()) {
            a.setPeakRankingPoints(a.getRankingPoints());
        }
        if (b.getRankingPoints() > b.getPeakRankingPoints()) {
            b.setPeakRankingPoints(b.getRankingPoints());
        }
        seasons.findByActiveTrue().ifPresent(season -> {
            upsert(a.getUser(), season, result.newA(), scoreA, scoreB);
            upsert(b.getUser(), season, result.newB(), scoreB, scoreA);
        });
        return result;
    }

    @Transactional
    public RankingSeasonEntity requireActive() {
        return seasons.findByActiveTrue().orElse(null);
    }

    private void upsert(UserEntity user, RankingSeasonEntity season, int points, int scored, int conceded) {
        RankingEntity ranking = rankings.findByUserIdAndSeasonId(user.getId(), season.getId()).orElseGet(() -> {
            RankingEntity created = new RankingEntity();
            created.setUser(user);
            created.setSeason(season);
            created.setPoints(1000);
            return created;
        });
        ranking.setPoints(points);
        ranking.setDivision(Division.fromPoints(points));
        ranking.setMatchesPlayed(ranking.getMatchesPlayed() + 1);
        if (scored > conceded) {
            ranking.setWins(ranking.getWins() + 1);
        } else if (scored < conceded) {
            ranking.setLosses(ranking.getLosses() + 1);
        } else {
            ranking.setDraws(ranking.getDraws() + 1);
        }
        ranking.setUpdatedAt(Instant.now());
        rankings.save(ranking);
    }
}
