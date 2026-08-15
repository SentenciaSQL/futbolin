package com.futbolin.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "question_options")
public class QuestionOptionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private QuestionEntity question;

    @Column(name = "option_key", nullable = false)
    private String optionKey;

    @Column(name = "text_es", nullable = false, length = 500)
    private String textEs;

    @Column(name = "text_en", nullable = false, length = 500)
    private String textEn;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
