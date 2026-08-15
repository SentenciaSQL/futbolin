package com.futbolin.data.repository;

import com.futbolin.data.entity.QuestionReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionReportRepository extends JpaRepository<QuestionReportEntity, UUID> {
    boolean existsByQuestionIdAndReporterIdAndStatus(UUID questionId, UUID reporterId, String status);
    List<QuestionReportEntity> findByStatusOrderByCreatedAtDesc(String status);
}
