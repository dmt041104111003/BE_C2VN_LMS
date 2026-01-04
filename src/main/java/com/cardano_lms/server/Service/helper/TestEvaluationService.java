package com.cardano_lms.server.Service.helper;

import com.cardano_lms.server.DTO.Request.AnswerSubmissionRequest;
import com.cardano_lms.server.DTO.Request.ProgressCreationRequest;
import com.cardano_lms.server.DTO.Request.TestSubmissionRequest;
import com.cardano_lms.server.DTO.Response.QuestionResultResponse;
import com.cardano_lms.server.DTO.Response.TestResultResponse;
import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Repository.UserAnswerRepository;
import com.cardano_lms.server.Service.ProgressService;
import com.cardano_lms.server.constant.CourseContentType;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestEvaluationService {

    private final UserAnswerRepository userAnswerRepository;
    private final ProgressService progressService;

    @Data
    @Builder
    public static class EvaluationConfig {
        private Long testId;
        private String testTitle;
        private int passScore;
        private User user;
        private Course course;
        private List<QuestionData> questions;
        private Map<Long, List<Long>> submittedAnswers;
        private boolean persistUserAnswers;
    }

    @Data
    @Builder
    public static class QuestionData {
        private Long id;
        private String content;
        private String explanation;
        private int score;
        private List<AnswerData> answers;
    }

    @Data
    @Builder
    public static class AnswerData {
        private Long id;
        private String content;
        private boolean correct;
    }

    public TestResultResponse evaluate(EvaluationConfig config) {
        int totalScore = 0;
        int maxScore = 0;
        int correctAnswersCount = 0;
        List<QuestionResultResponse> questionResults = new ArrayList<>();

        for (QuestionData question : config.getQuestions()) {
            maxScore += question.getScore();

            List<Long> selectedAnswerIds = config.getSubmittedAnswers()
                    .getOrDefault(question.getId(), Collections.emptyList());

            Set<Long> correctAnswerIds = question.getAnswers().stream()
                    .filter(AnswerData::isCorrect)
                    .map(AnswerData::getId)
                    .collect(Collectors.toSet());

            if (config.isPersistUserAnswers()) {
                persistUserAnswers(config, question, selectedAnswerIds);
            }

            boolean isCorrect = new HashSet<>(selectedAnswerIds).equals(correctAnswerIds);
            if (isCorrect) {
                totalScore += question.getScore();
                correctAnswersCount++;
            }

            questionResults.add(QuestionResultResponse.builder()
                    .questionId(question.getId())
                    .correctAnswerIds(new ArrayList<>(correctAnswerIds))
                    .selectedAnswerIds(selectedAnswerIds)
                    .isCorrect(isCorrect)
                    .explanation(question.getExplanation())
                    .build());
        }

        double scorePercent = calculateScorePercent(totalScore, maxScore);
        boolean passed = scorePercent >= config.getPassScore();

        if (passed && config.getCourse() != null) {
            progressService.createProgress(
                    ProgressCreationRequest.builder()
                            .testId(config.getTestId())
                            .score((int) scorePercent)
                            .type(CourseContentType.TEST)
                            .build(),
                    config.getUser().getId(),
                    config.getCourse().getId()
            );
        }

        return TestResultResponse.builder()
                .testId(config.getTestId())
                .userId(config.getUser().getId())
                .courseId(config.getCourse() != null ? config.getCourse().getId() : null)
                .totalQuestions(config.getQuestions().size())
                .correctAnswers(correctAnswersCount)
                .maxScore(maxScore)
                .score(scorePercent)
                .passScore(config.getPassScore())
                .passed(passed)
                .questionResults(questionResults)
                .build();
    }

    public Map<Long, List<Long>> buildSubmittedAnswersMap(TestSubmissionRequest submission) {
        return submission.getAnswers().stream()
                .collect(Collectors.toMap(
                        AnswerSubmissionRequest::getQuestionId,
                        AnswerSubmissionRequest::getAnswerId,
                        (existing, replacement) -> {
                            List<Long> merged = new ArrayList<>(existing);
                            merged.addAll(replacement);
                            return merged;
                        }
                ));
    }

    public QuestionData fromDbQuestion(Question question) {
        return QuestionData.builder()
                .id(question.getId())
                .content(question.getContent())
                .explanation(question.getExplanation())
                .score(question.getScore())
                .answers(question.getAnswers().stream()
                        .map(this::fromDbAnswer)
                        .collect(Collectors.toList()))
                .build();
    }

    public AnswerData fromDbAnswer(Answer answer) {
        return AnswerData.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .correct(answer.isCorrect())
                .build();
    }

    @SuppressWarnings("unchecked")
    public QuestionData fromSnapshotQuestion(Map<String, Object> qData) {
        List<Map<String, Object>> answersData = (List<Map<String, Object>>) 
                qData.getOrDefault("answers", Collections.emptyList());

        return QuestionData.builder()
                .id(((Number) qData.get("id")).longValue())
                .content((String) qData.get("content"))
                .explanation((String) qData.get("explanation"))
                .score(qData.get("score") != null ? ((Number) qData.get("score")).intValue() : 1)
                .answers(answersData.stream()
                        .map(this::fromSnapshotAnswer)
                        .collect(Collectors.toList()))
                .build();
    }

    public AnswerData fromSnapshotAnswer(Map<String, Object> aData) {
        return AnswerData.builder()
                .id(((Number) aData.get("id")).longValue())
                .content((String) aData.get("content"))
                .correct(Boolean.TRUE.equals(aData.get("correct")))
                .build();
    }

    private void persistUserAnswers(EvaluationConfig config, QuestionData question, List<Long> selectedAnswerIds) {
        Map<Long, AnswerData> answerMap = question.getAnswers().stream()
                .collect(Collectors.toMap(AnswerData::getId, a -> a));

        for (Long answerId : selectedAnswerIds) {
            AnswerData answer = answerMap.get(answerId);

            UserAnswer ua = UserAnswer.builder()
                    .user(config.getUser())
                    .testTitleSnapshot(config.getTestTitle())
                    .questionContentSnapshot(question.getContent())
                    .answerContentSnapshot(answer != null ? answer.getContent() : null)
                    .isCorrectSnapshot(answer != null && answer.isCorrect())
                    .build();

            userAnswerRepository.save(ua);
        }
    }

    private double calculateScorePercent(int totalScore, int maxScore) {
        return maxScore > 0 ? ((double) totalScore / maxScore) * 100 : 0;
    }
}
