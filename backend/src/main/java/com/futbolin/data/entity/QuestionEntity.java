package com.futbolin.data.entity;

import com.futbolin.domain.question.Difficulty;
import com.futbolin.domain.question.QuestionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private QuestionCategoryEntity category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(name = "prompt_es", nullable = false, length = 1000)
    private String promptEs;

    @Column(name = "prompt_en", nullable = false, length = 1000)
    private String promptEn;

    @Column(name = "explanation_es", length = 1000)
    private String explanationEs;

    @Column(name = "explanation_en", length = 1000)
    private String explanationEn;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "correct_key")
    private String correctKey;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "times_asked", nullable = false)
    private int timesAsked;

    @Column(name = "times_correct", nullable = false)
    private int timesCorrect;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<QuestionOptionEntity> options = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
