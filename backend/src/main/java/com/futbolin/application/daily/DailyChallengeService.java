package com.futbolin.application.daily;

import com.futbolin.application.question.QuestionPicker;
import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.data.entity.DailyChallengeAnswerEntity;
import com.futbolin.data.entity.DailyChallengeEntity;
import com.futbolin.data.entity.QuestionEntity;
import com.futbolin.data.repository.DailyChallengeAnswerRepository;
import com.futbolin.data.repository.DailyChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class DailyChallengeService {

    private final DailyChallengeRepository challenges;
    private final DailyChallengeAnswerRepository answers;
    private final QuestionPicker picker;

    public DailyChallengeService(
            DailyChallengeRepository challenges,
            DailyChallengeAnswerRepository answers,
            QuestionPicker picker
    ) {
        this.challenges = challenges;
        this.answers = answers;
        this.picker = picker;
    }

    @Transactional
    public DailyChallengeEntity today() {
        LocalDate date = LocalDate.now();
        return challenges.findByChallengeDate(date).orElseGet(() -> {
            QuestionPicker.PickedQuestion picked = picker.pick(null, null, java.util.List.of());
            DailyChallengeEntity created = new DailyChallengeEntity();
            created.setChallengeDate(date);
            created.setQuestion(picked.question());
            return challenges.save(created);
        });
    }

    @Transactional
    public Map<String, Object> answer(UUID userId, String optionKey) {
        DailyChallengeEntity challenge = today();
        if (answers.findByChallengeIdAndUserId(challenge.getId(), userId).isPresent()) {
            throw new ApiException(ErrorCode.ALREADY_ANSWERED);
        }
        QuestionEntity question = challenge.getQuestion();
        boolean correct = question.getOptions().stream()
                .anyMatch(o -> o.getOptionKey().equals(optionKey) && o.isCorrect());
        DailyChallengeAnswerEntity answer = new DailyChallengeAnswerEntity();
        answer.setChallenge(challenge);
        answer.setUserId(userId);
        answer.setOptionKey(optionKey);
        answer.setCorrect(correct);
        answers.save(answer);
        challenge.setTotalAnswers(challenge.getTotalAnswers() + 1);
        if (correct) {
            challenge.setCorrectAnswers(challenge.getCorrectAnswers() + 1);
        }
        double pct = challenge.getTotalAnswers() == 0 ? 0 : (100.0 * challenge.getCorrectAnswers() / challenge.getTotalAnswers());
        return Map.of(
                "correct", correct,
                "correctKey", question.getCorrectKey() == null ? "" : question.getCorrectKey(),
                "explanationEs", question.getExplanationEs() == null ? "" : question.getExplanationEs(),
                "explanationEn", question.getExplanationEn() == null ? "" : question.getExplanationEn(),
                "globalCorrectPercent", pct,
                "totalAnswers", challenge.getTotalAnswers()
        );
    }
}
