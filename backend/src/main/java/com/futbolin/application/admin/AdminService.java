package com.futbolin.application.admin;

import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.data.entity.QuestionCategoryEntity;
import com.futbolin.data.entity.QuestionEntity;
import com.futbolin.data.entity.QuestionOptionEntity;
import com.futbolin.data.entity.QuestionReportEntity;
import com.futbolin.data.repository.MatchRepository;
import com.futbolin.data.repository.QuestionCategoryRepository;
import com.futbolin.data.repository.QuestionReportRepository;
import com.futbolin.data.repository.QuestionRepository;
import com.futbolin.data.repository.UserRepository;
import com.futbolin.domain.match.MatchStatus;
import com.futbolin.domain.question.Difficulty;
import com.futbolin.domain.question.QuestionType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    private final QuestionRepository questions;
    private final QuestionCategoryRepository categories;
    private final QuestionReportRepository reports;
    private final UserRepository users;
    private final MatchRepository matches;

    public AdminService(
            QuestionRepository questions,
            QuestionCategoryRepository categories,
            QuestionReportRepository reports,
            UserRepository users,
            MatchRepository matches
    ) {
        this.questions = questions;
        this.categories = categories;
        this.reports = reports;
        this.users = users;
        this.matches = matches;
    }

    public Map<String, Object> dashboard() {
        return Map.of(
                "users", users.count(),
                "questions", questions.count(),
                "liveMatches", matches.countByStatus(MatchStatus.LIVE),
                "openReports", reports.findByStatusOrderByCreatedAtDesc("OPEN").size()
        );
    }

    @Transactional
    public QuestionEntity createQuestion(QuestionDraft draft) {
        if (questions.existsByPromptEsIgnoreCase(draft.promptEs())) {
            throw new ApiException(ErrorCode.CONFLICT, "Duplicate question");
        }
        QuestionCategoryEntity category = categories.findByCode(draft.categoryCode())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Category not found"));
        QuestionEntity q = new QuestionEntity();
        q.setCategory(category);
        q.setType(QuestionType.valueOf(draft.type()));
        q.setDifficulty(Difficulty.valueOf(draft.difficulty()));
        q.setPromptEs(draft.promptEs());
        q.setPromptEn(draft.promptEn());
        q.setExplanationEs(draft.explanationEs());
        q.setExplanationEn(draft.explanationEn());
        q.setImageUrl(draft.imageUrl());
        q.setCorrectKey(draft.correctAnswer());
        q.setActive(true);
        for (int i = 0; i < draft.options().size(); i++) {
            OptionDraft opt = draft.options().get(i);
            QuestionOptionEntity o = new QuestionOptionEntity();
            o.setQuestion(q);
            o.setOptionKey(opt.key());
            o.setTextEs(opt.textEs());
            o.setTextEn(opt.textEn());
            o.setCorrect(opt.key().equalsIgnoreCase(draft.correctAnswer()));
            o.setSortOrder(i);
            q.getOptions().add(o);
        }
        return questions.save(q);
    }

    @Transactional
    public Map<String, Object> importFile(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        int imported = 0;
        int duplicates = 0;
        List<String> errors = new ArrayList<>();
        try {
            if (name.endsWith(".json")) {
                var tree = new com.fasterxml.jackson.databind.ObjectMapper().readTree(file.getInputStream());
                for (var node : tree) {
                    Result r = importRow(
                            text(node, "question"), text(node, "question_en"),
                            text(node, "option_a"), text(node, "option_b"), text(node, "option_c"), text(node, "option_d"),
                            text(node, "correct_answer"), text(node, "category"), text(node, "difficulty"),
                            text(node, "explanation")
                    );
                    imported += r.imported;
                    duplicates += r.duplicates;
                    errors.addAll(r.errors);
                }
            } else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
                    Sheet sheet = wb.getSheetAt(0);
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }
                        Result r = importRow(
                                cell(row, 0), cell(row, 1), cell(row, 2), cell(row, 3), cell(row, 4), cell(row, 5),
                                cell(row, 6), cell(row, 7), cell(row, 8), cell(row, 9)
                        );
                        imported += r.imported;
                        duplicates += r.duplicates;
                        errors.addAll(r.errors);
                    }
                }
            } else {
                try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true)
                        .build().parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    for (CSVRecord rec : parser) {
                        Result r = importRow(
                                rec.get("question"), rec.isMapped("question_en") ? rec.get("question_en") : rec.get("question"),
                                rec.get("option_a"), rec.get("option_b"), rec.get("option_c"), rec.get("option_d"),
                                rec.get("correct_answer"), rec.get("category"), rec.get("difficulty"),
                                rec.isMapped("explanation") ? rec.get("explanation") : ""
                        );
                        imported += r.imported;
                        duplicates += r.duplicates;
                        errors.addAll(r.errors);
                    }
                }
            }
        } catch (Exception e) {
            throw new ApiException(ErrorCode.IMPORT_FAILED, e.getMessage());
        }
        return Map.of("imported", imported, "duplicates", duplicates, "errors", errors);
    }

    private Result importRow(
            String question, String questionEn, String a, String b, String c, String d,
            String correct, String category, String difficulty, String explanation
    ) {
        if (question == null || question.isBlank()) {
            return new Result(0, 0, List.of("Empty question"));
        }
        if (questions.existsByPromptEsIgnoreCase(question.trim())) {
            return new Result(0, 1, List.of());
        }
        try {
            createQuestion(new QuestionDraft(
                    category == null ? "WORLD_CUP" : category.trim().toUpperCase().replace(' ', '_'),
                    "MULTIPLE_CHOICE",
                    difficulty == null ? "MEDIUM" : difficulty.trim().toUpperCase(),
                    question.trim(),
                    (questionEn == null || questionEn.isBlank()) ? question.trim() : questionEn.trim(),
                    explanation,
                    explanation,
                    null,
                    correct == null ? "A" : correct.trim().toUpperCase(),
                    List.of(
                            new OptionDraft("A", a, a),
                            new OptionDraft("B", b, b),
                            new OptionDraft("C", c, c),
                            new OptionDraft("D", d, d)
                    )
            ));
            return new Result(1, 0, List.of());
        } catch (Exception e) {
            return new Result(0, 0, List.of(e.getMessage()));
        }
    }

    @Transactional
    public void resolveReport(UUID id, String status, String note) {
        QuestionReportEntity report = reports.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        report.setStatus(status);
        report.setAdminNote(note);
        report.setResolvedAt(Instant.now());
    }

    @Transactional
    public void lockUser(UUID id, boolean locked) {
        var user = users.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        user.setLocked(locked);
    }

    private String text(com.fasterxml.jackson.databind.JsonNode node, String field) {
        return node.path(field).asText("");
    }

    private String cell(Row row, int i) {
        var cell = row.getCell(i);
        return cell == null ? "" : cell.toString();
    }

    public record QuestionDraft(
            String categoryCode,
            String type,
            String difficulty,
            String promptEs,
            String promptEn,
            String explanationEs,
            String explanationEn,
            String imageUrl,
            String correctAnswer,
            List<OptionDraft> options
    ) {}

    public record OptionDraft(String key, String textEs, String textEn) {}

    private record Result(int imported, int duplicates, List<String> errors) {}
}
