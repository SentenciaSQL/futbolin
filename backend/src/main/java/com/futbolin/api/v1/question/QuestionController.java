package com.futbolin.api.v1.question;

import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.entity.QuestionReportEntity;
import com.futbolin.data.repository.QuestionCategoryRepository;
import com.futbolin.data.repository.QuestionReportRepository;
import com.futbolin.data.repository.QuestionRepository;
import com.futbolin.data.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionCategoryRepository categories;
    private final QuestionRepository questions;
    private final QuestionReportRepository reports;
    private final UserRepository users;

    public QuestionController(
            QuestionCategoryRepository categories,
            QuestionRepository questions,
            QuestionReportRepository reports,
            UserRepository users
    ) {
        this.categories = categories;
        this.questions = questions;
        this.reports = reports;
        this.users = users;
    }

    @GetMapping("/categories")
    public Object categories() {
        return categories.findByActiveTrueOrderBySortOrderAsc();
    }

    @PostMapping("/{id}/report")
    public void report(@AuthenticationPrincipal UserPrincipal principal,
                       @PathVariable UUID id,
                       @RequestBody ReportRequest request) {
        if (reports.existsByQuestionIdAndReporterIdAndStatus(id, principal.id(), "OPEN")) {
            throw new ApiException(ErrorCode.QUESTION_REPORTED);
        }
        QuestionReportEntity report = new QuestionReportEntity();
        report.setQuestion(questions.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND)));
        report.setReporter(users.getReferenceById(principal.id()));
        report.setReason(request.reason());
        report.setDetails(request.details());
        reports.save(report);
    }

    public record ReportRequest(@NotBlank String reason, String details) {}
}
