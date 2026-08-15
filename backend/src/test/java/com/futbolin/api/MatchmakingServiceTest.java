package com.futbolin.api;

import com.futbolin.application.match.MatchmakingService;
import com.futbolin.application.match.MatchmakingService.Pair;
import com.futbolin.data.entity.UserEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.UserProfileRepository;
import com.futbolin.data.repository.UserRepository;
import com.futbolin.domain.ranking.Division;
import com.futbolin.domain.user.AuthProvider;
import com.futbolin.domain.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchmakingServiceTest {

    @Autowired
    MatchmakingService matchmakingService;
    @Autowired
    UserRepository users;
    @Autowired
    UserProfileRepository profiles;

    @Test
    void pairsPlayersWithSimilarRating() {
        UserEntity a = user("alpha_mm", 1100);
        UserEntity b = user("bravo_mm", 1120);
        matchmakingService.enqueue(a.getId(), 40);
        matchmakingService.enqueue(b.getId(), 55);
        Optional<Pair> pair = matchmakingService.tryMatch(a.getId());
        assertTrue(pair.isPresent());
    }

    private UserEntity user(String username, int points) {
        UserEntity user = new UserEntity();
        user.setEmail(username + "@futbolin.app");
        user.setUsername(username);
        user.setPasswordHash("x");
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUser(user);
        profile.setDisplayName(username);
        profile.setRankingPoints(points);
        profile.setDivision(Division.fromPoints(points));
        user.setProfile(profile);
        return users.save(user);
    }
}
