package com.futbolin.application.survival;

import com.futbolin.application.question.QuestionPicker;
import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.data.entity.QuestionEntity;
import com.futbolin.data.entity.SurvivalRunEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.QuestionRepository;
import com.futbolin.data.repository.SurvivalRunRepository;
import com.futbolin.data.repository.UserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SurvivalService {

    private final Map<UUID, Run> runs = new ConcurrentHashMap<>();
    private final QuestionPicker picker;
    private final QuestionRepository questions;
    private final SurvivalRunRepository survivalRuns;
    private final UserProfileRepository profiles;

    public SurvivalService(
            QuestionPicker picker,
            QuestionRepository questions,
            SurvivalRunRepository survivalRuns,
            UserProfileRepository profiles
    ) {
        this.picker = picker;
        this.questions = questions;
        this.survivalRuns = survivalRuns;
        this.profiles = profiles;
    }

    @Transactional
    public Map<String, Object> start(UUID userId) {
        QuestionPicker.PickedQuestion picked = picker.pick(null, null, List.of());
        runs.put(userId, new Run(0, picked.question().getId(), List.of(picked.question().getId())));
        return Map.of("score", 0, "alive", true, "question", questionMap(picked));
    }

    @Transactional
    public Map<String, Object> answer(UUID userId, String optionKey) {
        Run run = runs.get(userId);
        if (run == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "No survival run");
        }
        QuestionEntity current = questions.findWithOptions(run.questionId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        boolean correct = current.getOptions().stream()
                .anyMatch(o -> o.getOptionKey().equals(optionKey) && o.isCorrect());
        if (!correct) {
            SurvivalRunEntity saved = new SurvivalRunEntity();
            saved.setUserId(userId);
            saved.setScore(run.score());
            survivalRuns.save(saved);
            UserProfileEntity profile = profiles.findById(userId).orElseThrow();
            if (run.score() > profile.getSurvivalBest()) {
                profile.setSurvivalBest(run.score());
            }
            runs.remove(userId);
            return Map.of(
                    "alive", false,
                    "score", run.score(),
                    "best", profile.getSurvivalBest(),
                    "correctKey", current.getCorrectKey() == null ? "" : current.getCorrectKey()
            );
        }
        List<UUID> used = new ArrayList<>(run.used());
        QuestionPicker.PickedQuestion next = picker.pick(null, null, used);
        used.add(next.question().getId());
        int nextScore = run.score() + 1;
        runs.put(userId, new Run(nextScore, next.question().getId(), used));
        return Map.of("alive", true, "score", nextScore, "question", questionMap(next));
    }

    public Object leaderboard() {
        return survivalRuns.findAllByOrderByScoreDescCreatedAtAsc(PageRequest.of(0, 50));
    }

    private Map<String, Object> questionMap(QuestionPicker.PickedQuestion picked) {
        return Map.of(
                "id", picked.question().getId(),
                "promptEs", picked.question().getPromptEs(),
                "promptEn", picked.question().getPromptEn(),
                "type", picked.question().getType().name(),
                "options", picked.shuffledOptions().stream().map(o -> Map.of(
                        "key", o.getOptionKey(),
                        "textEs", o.getTextEs(),
                        "textEn", o.getTextEn()
                )).toList()
        );
    }

    private record Run(int score, UUID questionId, List<UUID> used) {}
}
