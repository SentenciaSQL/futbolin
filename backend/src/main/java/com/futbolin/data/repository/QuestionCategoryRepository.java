package com.futbolin.data.repository;

import com.futbolin.data.entity.QuestionCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionCategoryRepository extends JpaRepository<QuestionCategoryEntity, UUID> {
    Optional<QuestionCategoryEntity> findByCode(String code);
    List<QuestionCategoryEntity> findByActiveTrueOrderBySortOrderAsc();
}
