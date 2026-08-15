package com.futbolin.application.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futbolin.core.props.AppProperties;
import com.futbolin.data.entity.*;
import com.futbolin.data.repository.*;
import com.futbolin.domain.question.Difficulty;
import com.futbolin.domain.question.QuestionType;
import com.futbolin.domain.ranking.Division;
import com.futbolin.domain.user.AuthProvider;
import com.futbolin.domain.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository users;
    private final QuestionCategoryRepository categories;
    private final QuestionRepository questions;
    private final AchievementRepository achievements;
    private final MissionRepository missions;
    private final CosmeticRepository cosmetics;
    private final RankingSeasonRepository seasons;
    private final DailyLoginRewardRepository dailyRewards;
    private final ObjectMapper objectMapper;

    public DataSeeder(
            AppProperties properties,
            PasswordEncoder passwordEncoder,
            UserRepository users,
            QuestionCategoryRepository categories,
            QuestionRepository questions,
            AchievementRepository achievements,
            MissionRepository missions,
            CosmeticRepository cosmetics,
            RankingSeasonRepository seasons,
            DailyLoginRewardRepository dailyRewards,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
        this.users = users;
        this.categories = categories;
        this.questions = questions;
        this.achievements = achievements;
        this.missions = missions;
        this.cosmetics = cosmetics;
        this.seasons = seasons;
        this.dailyRewards = dailyRewards;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        seedAdmin();
        Map<String, QuestionCategoryEntity> byCode = seedCategories();
        seedQuestions(byCode);
        seedAchievements();
        seedMissions();
        seedCosmetics();
        seedSeason();
        seedDailyRewards();
    }

    private void seedAdmin() {
        if (users.findByEmail(properties.seed().adminEmail()).isPresent()) {
            return;
        }
        UserEntity admin = new UserEntity();
        admin.setEmail(properties.seed().adminEmail());
        admin.setUsername(properties.seed().adminUsername());
        admin.setPasswordHash(passwordEncoder.encode(properties.seed().adminPassword()));
        admin.setRole(Role.ADMIN);
        admin.setProvider(AuthProvider.LOCAL);
        admin.setEmailVerified(true);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUser(admin);
        profile.setDisplayName("Director Técnico");
        profile.setDivision(Division.LEGEND);
        profile.setRankingPoints(2200);
        profile.setPeakRankingPoints(2200);
        profile.setLevel(20);
        admin.setProfile(profile);
        users.save(admin);
        log.info("Seeded admin {}", admin.getEmail());
    }

    private Map<String, QuestionCategoryEntity> seedCategories() {
        Map<String, QuestionCategoryEntity> byCode = new java.util.HashMap<>();
        if (categories.count() > 0) {
            categories.findAll().forEach(c -> byCode.put(c.getCode(), c));
            return byCode;
        }
        List<String[]> rows = List.of(
                new String[]{"WORLD_CUP", "Mundiales", "World Cups"},
                new String[]{"CHAMPIONS", "Champions League", "Champions League"},
                new String[]{"LIBERTADORES", "Copa Libertadores", "Copa Libertadores"},
                new String[]{"COPA_AMERICA", "Copa América", "Copa América"},
                new String[]{"EURO", "Eurocopa", "European Championship"},
                new String[]{"PREMIER", "Premier League", "Premier League"},
                new String[]{"LALIGA", "LaLiga", "LaLiga"},
                new String[]{"SERIE_A", "Serie A", "Serie A"},
                new String[]{"BUNDESLIGA", "Bundesliga", "Bundesliga"},
                new String[]{"LIGUE1", "Ligue 1", "Ligue 1"},
                new String[]{"MLS", "MLS", "MLS"},
                new String[]{"LIGA_MX", "Liga MX", "Liga MX"},
                new String[]{"LATAM", "Fútbol latinoamericano", "Latin American football"},
                new String[]{"NATIONAL_TEAMS", "Selecciones nacionales", "National teams"},
                new String[]{"HISTORIC_CLUBS", "Clubes históricos", "Historic clubs"},
                new String[]{"HISTORIC_PLAYERS", "Jugadores históricos", "Historic players"},
                new String[]{"CURRENT_PLAYERS", "Jugadores actuales", "Current players"},
                new String[]{"COACHES", "Entrenadores", "Coaches"},
                new String[]{"RECORDS", "Récords", "Records"},
                new String[]{"STADIUMS", "Estadios", "Stadiums"},
                new String[]{"RULES", "Reglas del fútbol", "Laws of the game"},
                new String[]{"BALLON_DOR", "Balón de Oro", "Ballon d'Or"},
                new String[]{"TRANSFERS", "Transferencias históricas", "Historic transfers"},
                new String[]{"TRIVIA", "Curiosidades", "Trivia"},
                new String[]{"CRESTS", "Escudos", "Crests"},
                new String[]{"KITS", "Camisetas", "Kits"},
                new String[]{"GUESS_PLAYER", "Adivina el jugador", "Guess the player"},
                new String[]{"GUESS_CLUB", "Adivina el club", "Guess the club"}
        );
        int i = 0;
        for (String[] row : rows) {
            QuestionCategoryEntity c = new QuestionCategoryEntity();
            c.setCode(row[0]);
            c.setNameEs(row[1]);
            c.setNameEn(row[2]);
            c.setSortOrder(i++);
            categories.save(c);
            byCode.put(c.getCode(), c);
        }
        return byCode;
    }

    private void seedQuestions(Map<String, QuestionCategoryEntity> byCode) throws Exception {
        if (questions.count() > 0) {
            return;
        }
        JsonNode array = objectMapper.readTree(new ClassPathResource("seed/questions.json").getInputStream());
        for (JsonNode node : array) {
            QuestionCategoryEntity category = byCode.get(node.path("category").asText("WORLD_CUP"));
            if (category == null) {
                log.warn("Skipping question with unknown category {}", node.path("category").asText());
                continue;
            }
            QuestionEntity q = new QuestionEntity();
            q.setCategory(category);
            q.setType(QuestionType.valueOf(node.path("type").asText("MULTIPLE_CHOICE")));
            q.setDifficulty(Difficulty.valueOf(node.path("difficulty").asText("MEDIUM")));
            q.setPromptEs(node.get("promptEs").asText());
            q.setPromptEn(node.get("promptEn").asText());
            q.setExplanationEs(node.path("explanationEs").asText(null));
            q.setExplanationEn(node.path("explanationEn").asText(null));
            q.setImageUrl(node.path("imageUrl").asText(null));
            q.setMetadataJson(node.path("metadata").isMissingNode() ? null : node.get("metadata").toString());
            q.setCorrectKey(node.path("correct").asText("A"));
            int i = 0;
            for (JsonNode opt : node.get("options")) {
                QuestionOptionEntity o = new QuestionOptionEntity();
                o.setQuestion(q);
                o.setOptionKey(opt.get("key").asText());
                o.setTextEs(opt.get("es").asText());
                o.setTextEn(opt.get("en").asText());
                o.setCorrect(opt.get("key").asText().equals(q.getCorrectKey()));
                o.setSortOrder(i++);
                q.getOptions().add(o);
            }
            questions.save(q);
        }
        log.info("Seeded {} questions", questions.count());
    }

    private void seedAchievements() {
        if (achievements.count() > 0) {
            return;
        }
        saveAchievement("FIRST_GOAL", "Primer Gol", "First Goal", "Marca tu primer gol.", "Score your first goal.");
        saveAchievement("HAT_TRICK", "Hat-Trick", "Hat-Trick", "Marca tres goles en una partida.", "Score three goals in one match.");
        saveAchievement("UNSTOPPABLE", "Imparable", "Unstoppable", "Gana cinco partidas consecutivas.", "Win five matches in a row.");
        saveAchievement("ENCYCLOPEDIA", "Enciclopedia del fútbol", "Football Encyclopedia", "Responde correctamente 1000 preguntas.", "Answer 1000 questions correctly.");
        saveAchievement("WORLD_CHAMPION", "Campeón del Mundo", "World Champion", "Alcanza la división Leyenda.", "Reach the Legend division.");
    }

    private void saveAchievement(String code, String es, String en, String des, String den) {
        AchievementEntity a = new AchievementEntity();
        a.setCode(code);
        a.setNameEs(es);
        a.setNameEn(en);
        a.setDescriptionEs(des);
        a.setDescriptionEn(den);
        a.setXpReward(80);
        a.setCoinsReward(40);
        achievements.save(a);
    }

    private void seedMissions() {
        if (missions.count() > 0) {
            return;
        }
        saveMission("DAILY_PLAY_3", "DAILY", "Juega 3 partidas", "Play 3 matches", "PLAY_MATCH", 3, 40, 20);
        saveMission("DAILY_WIN_2", "DAILY", "Gana 2 partidas", "Win 2 matches", "WIN_MATCH", 2, 60, 30);
        saveMission("DAILY_CORRECT_20", "DAILY", "Acierta 20 preguntas", "Answer 20 correctly", "CORRECT_ANSWERS", 20, 50, 25);
        saveMission("DAILY_GOALS_5", "DAILY", "Marca 5 goles", "Score 5 goals", "SCORE_GOALS", 5, 50, 25);
        saveMission("DAILY_STREAK_5", "DAILY", "Racha de 5 aciertos", "5-answer streak", "ANSWER_STREAK", 5, 40, 20);
        saveMission("WEEKLY_PLAY_15", "WEEKLY", "Juega 15 partidas", "Play 15 matches", "PLAY_MATCH", 15, 200, 100);
    }

    private void saveMission(String code, String period, String es, String en, String metric, int target, int xp, int coins) {
        MissionEntity m = new MissionEntity();
        m.setCode(code);
        m.setPeriod(period);
        m.setNameEs(es);
        m.setNameEn(en);
        m.setDescriptionEs(es);
        m.setDescriptionEn(en);
        m.setMetric(metric);
        m.setTarget(target);
        m.setXpReward(xp);
        m.setCoinsReward(coins);
        missions.save(m);
    }

    private void seedCosmetics() {
        if (cosmetics.count() > 0) {
            return;
        }
        saveCosmetic("default", "AVATAR", "Clásico", "Classic", "COMMON", 0, 1);
        saveCosmetic("striker", "AVATAR", "Delantero", "Striker", "RARE", 200, 3);
        saveCosmetic("professor", "TITLE", "El Profesor", "The Professor", "RARE", 250, 5);
        saveCosmetic("world_king", "TITLE", "Rey del Mundial", "World Cup King", "EPIC", 400, 10);
        saveCosmetic("ucl_expert", "TITLE", "Experto en Champions", "Champions Expert", "EPIC", 400, 10);
        saveCosmetic("gold_frame", "FRAME", "Marco de oro", "Gold frame", "RARE", 180, 4);
        saveCosmetic("classic_ball", "BALL", "Balón clásico", "Classic ball", "COMMON", 80, 1);
        saveCosmetic("night_stadium", "STADIUM", "Estadio nocturno", "Night stadium", "EPIC", 500, 8);
        saveCosmetic("confetti", "CELEBRATION", "Confeti", "Confetti", "RARE", 150, 2);
        saveCosmetic("fireworks", "CELEBRATION", "Fuegos artificiales", "Fireworks", "EPIC", 300, 6);
    }

    private void saveCosmetic(String key, String type, String es, String en, String rarity, int price, int level) {
        CosmeticEntity c = new CosmeticEntity();
        c.setKey(key.trim());
        c.setType(type);
        c.setNameEs(es);
        c.setNameEn(en);
        c.setRarity(rarity);
        c.setPriceCoins(price);
        c.setMinLevel(level);
        cosmetics.save(c);
    }

    private void seedSeason() {
        if (seasons.count() > 0) {
            return;
        }
        RankingSeasonEntity season = new RankingSeasonEntity();
        season.setName("Temporada 1 – Camino a la Gloria");
        season.setSlug("season-1-glory-road");
        season.setStartsAt(Instant.now());
        season.setEndsAt(Instant.now().plus(30, ChronoUnit.DAYS));
        season.setActive(true);
        seasons.save(season);
    }

    private void seedDailyRewards() {
        if (dailyRewards.count() > 0) {
            return;
        }
        int[] coins = {20, 25, 0, 40, 0, 50, 120};
        int[] xp = {10, 15, 40, 20, 25, 30, 80};
        String[] cosmetic = {null, null, null, null, "striker", null, "gold_frame"};
        for (int i = 0; i < 7; i++) {
            DailyLoginRewardEntity r = new DailyLoginRewardEntity();
            r.setDayIndex(i + 1);
            r.setCoins(coins[i]);
            r.setXp(xp[i]);
            r.setCosmeticKey(cosmetic[i]);
            dailyRewards.save(r);
        }
    }
}
