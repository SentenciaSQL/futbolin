package com.futbolin.data.repository;

import com.futbolin.data.entity.QuestionEntity;
import com.futbolin.domain.question.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {

    Page<QuestionEntity> findByActiveTrue(Pageable pageable);

    boolean existsByPromptEsIgnoreCase(String promptEs);

    @Query("""
            SELECT q.id FROM QuestionEntity q
            WHERE q.active = true
              AND (:difficulty IS NULL OR q.difficulty = :difficulty)
              AND (:categoryId IS NULL OR q.category.id = :categoryId)
            """)
    List<UUID> findActiveIds(@Param("difficulty") Difficulty difficulty, @Param("categoryId") UUID categoryId);

    @Query("SELECT q FROM QuestionEntity q JOIN FETCH q.options JOIN FETCH q.category WHERE q.id = :id")
    Optional<QuestionEntity> findWithOptions(@Param("id") UUID id);

    long countByActiveTrue();
}
