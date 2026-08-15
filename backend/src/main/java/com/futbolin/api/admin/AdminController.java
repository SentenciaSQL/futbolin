package com.futbolin.api.admin;

import com.futbolin.application.admin.AdminService;
import com.futbolin.data.entity.*;
import com.futbolin.data.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final QuestionRepository questions;
    private final QuestionCategoryRepository categories;
    private final QuestionReportRepository reports;
    private final UserRepository users;
    private final MatchRepository matches;
    private final RankingSeasonRepository seasons;
    private final MissionRepository missions;
    private final CosmeticRepository cosmetics;

    public AdminController(
            AdminService adminService,
            QuestionRepository questions,
            QuestionCategoryRepository categories,
            QuestionReportRepository reports,
            UserRepository users,
            MatchRepository matches,
            RankingSeasonRepository seasons,
            MissionRepository missions,
            CosmeticRepository cosmetics
    ) {
        this.adminService = adminService;
        this.questions = questions;
        this.categories = categories;
        this.reports = reports;
        this.users = users;
        this.matches = matches;
        this.seasons = seasons;
        this.missions = missions;
        this.cosmetics = cosmetics;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/questions")
    public Object questions(@RequestParam(defaultValue = "0") int page) {
        return questions.findAll(PageRequest.of(page, 50));
    }

    @PostMapping("/questions")
    public QuestionEntity createQuestion(@RequestBody AdminService.QuestionDraft draft) {
        return adminService.createQuestion(draft);
    }

    @DeleteMapping("/questions/{id}")
    public void deleteQuestion(@PathVariable UUID id) {
        questions.deleteById(id);
    }

    @PostMapping(value = "/questions/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> importQuestions(@RequestPart("file") MultipartFile file) {
        return adminService.importFile(file);
    }

    @GetMapping("/categories")
    public Object categories() {
        return categories.findAll();
    }

    @PostMapping("/categories")
    public QuestionCategoryEntity createCategory(@RequestBody QuestionCategoryEntity body) {
        return categories.save(body);
    }

    @GetMapping("/reports")
    public Object reports() {
        return reports.findByStatusOrderByCreatedAtDesc("OPEN");
    }

    @PostMapping("/reports/{id}/resolve")
    public void resolve(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        adminService.resolveReport(id, body.getOrDefault("status", "RESOLVED"), body.get("note"));
    }

    @GetMapping("/users")
    public Object users(@RequestParam(defaultValue = "0") int page) {
        return users.findAll(PageRequest.of(page, 50));
    }

    @PostMapping("/users/{id}/lock")
    public void lock(@PathVariable UUID id, @RequestParam boolean locked) {
        adminService.lockUser(id, locked);
    }

    @GetMapping("/matches")
    public Object matches(@RequestParam(defaultValue = "0") int page) {
        return matches.findAll(PageRequest.of(page, 50));
    }

    @GetMapping("/seasons")
    public Object seasons() {
        return seasons.findAll();
    }

    @PostMapping("/seasons")
    public RankingSeasonEntity createSeason(@RequestBody RankingSeasonEntity season) {
        season.setCreatedAt(Instant.now());
        return seasons.save(season);
    }

    @PostMapping("/missions")
    public MissionEntity createMission(@RequestBody MissionEntity mission) {
        return missions.save(mission);
    }

    @PostMapping("/cosmetics")
    public CosmeticEntity createCosmetic(@RequestBody CosmeticEntity cosmetic) {
        return cosmetics.save(cosmetic);
    }
}
