package com.futbolin.application.question;

import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.data.entity.QuestionEntity;
import com.futbolin.data.entity.QuestionOptionEntity;
import com.futbolin.data.repository.QuestionRepository;
import com.futbolin.domain.question.Difficulty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class QuestionPicker {

    private final QuestionRepository questions;

    public QuestionPicker(QuestionRepository questions) {
        this.questions = questions;
    }

    @Transactional
    public PickedQuestion pick(Difficulty difficulty, UUID categoryId, List<UUID> exclude) {
        List<UUID> ids = questions.findActiveIds(difficulty, categoryId);
        List<UUID> blocked = exclude == null ? List.of() : exclude;
        List<UUID> pool = ids.stream().filter(id -> !blocked.contains(id)).toList();
        if (pool.isEmpty()) {
            pool = questions.findActiveIds(null, null).stream().filter(id -> !blocked.contains(id)).toList();
        }
        if (pool.isEmpty()) {
            pool = questions.findActiveIds(null, null);
        }
        if (pool.isEmpty()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "No questions available");
        }
        UUID id = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        QuestionEntity question = questions.findWithOptions(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        question.setTimesAsked(question.getTimesAsked() + 1);
        List<QuestionOptionEntity> options = new ArrayList<>(question.getOptions());
        Collections.shuffle(options, ThreadLocalRandom.current());
        String shuffled = options.stream().map(QuestionOptionEntity::getOptionKey).collect(Collectors.joining(","));
        return new PickedQuestion(question, options, shuffled);
    }

    public Difficulty adaptive(int rankingPoints, double accuracy, int currentStreak) {
        if (rankingPoints >= 2000 || (accuracy > 0.8 && currentStreak >= 4)) {
            return Difficulty.EXPERT;
        }
        if (rankingPoints >= 1600 || accuracy > 0.7) {
            return Difficulty.HARD;
        }
        if (rankingPoints >= 1200 || accuracy > 0.5) {
            return Difficulty.MEDIUM;
        }
        return Difficulty.EASY;
    }

    public record PickedQuestion(QuestionEntity question, List<QuestionOptionEntity> shuffledOptions, String shuffledKeys) {}
}
