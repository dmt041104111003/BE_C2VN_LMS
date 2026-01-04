package com.cardano_lms.server.Service.helper;

import com.cardano_lms.server.DTO.Response.AnswerResponse;
import com.cardano_lms.server.DTO.Response.ChapterSummaryResponse;
import com.cardano_lms.server.DTO.Response.CourseDetailResponse;
import com.cardano_lms.server.DTO.Response.QuestionResponse;
import com.cardano_lms.server.DTO.Response.TestResponse;
import com.cardano_lms.server.Entity.Answer;
import com.cardano_lms.server.Entity.Chapter;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.Question;
import com.cardano_lms.server.Entity.Test;
import com.cardano_lms.server.Entity.UserAnswer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AnswerVisibilityHandler {

    private AnswerVisibilityHandler() {}

    public static void applyVisibility(Course course, CourseDetailResponse response, boolean canSeeCorrectAnswers, List<UserAnswer> userAnswers) {
        Map<Long, Set<Long>> userAnswerMap = buildUserAnswerMap(userAnswers);
        applyChapterTestsVisibility(course, response, canSeeCorrectAnswers, userAnswerMap);
        applyCourseTestsVisibility(course, response, canSeeCorrectAnswers, userAnswerMap);
    }
    
    private static Map<Long, Set<Long>> buildUserAnswerMap(List<UserAnswer> userAnswers) {
        if (userAnswers == null || userAnswers.isEmpty()) {
            return Collections.emptyMap();
        }
        return userAnswers.stream()
                .filter(ua -> ua.getQuestion() != null && ua.getAnswer() != null)
                .collect(Collectors.groupingBy(
                        ua -> ua.getQuestion().getId(),
                        Collectors.mapping(ua -> ua.getAnswer().getId(), Collectors.toSet())
                ));
    }

    private static void applyChapterTestsVisibility(Course course, CourseDetailResponse response, boolean canSee, Map<Long, Set<Long>> userAnswerMap) {
        if (response.getChapters() == null) return;

        Map<Long, Chapter> chapterEntityMap = buildEntityMap(course.getChapters(), Chapter::getId);

        for (ChapterSummaryResponse chapterResp : response.getChapters()) {
            if (chapterResp.getId() == null) continue;
            
            Chapter chapterEntity = chapterEntityMap.get(chapterResp.getId());
            List<Test> testEntities = chapterEntity != null ? chapterEntity.getTests() : null;
            applyTestListVisibility(chapterResp.getTests(), testEntities, canSee, userAnswerMap);
        }
    }

    private static void applyCourseTestsVisibility(Course course, CourseDetailResponse response, boolean canSee, Map<Long, Set<Long>> userAnswerMap) {
        applyTestListVisibility(response.getCourseTests(), course.getCourseTests(), canSee, userAnswerMap);
    }

    private static void applyTestListVisibility(List<TestResponse> testResponses, List<Test> testEntities, boolean canSee, Map<Long, Set<Long>> userAnswerMap) {
        if (testResponses == null) return;

        Map<Long, Test> testEntityMap = buildEntityMap(testEntities, Test::getId);

        for (TestResponse testResp : testResponses) {
            if (testResp.getId() == null) continue;
            
            Test testEntity = testEntityMap.get(testResp.getId());
            List<Question> questionEntities = testEntity != null ? testEntity.getQuestions() : null;
            applyQuestionListVisibility(testResp.getQuestions(), questionEntities, canSee, userAnswerMap);
        }
    }

    private static void applyQuestionListVisibility(List<QuestionResponse> questionResponses, List<Question> questionEntities, boolean canSee, Map<Long, Set<Long>> userAnswerMap) {
        if (questionResponses == null) return;

        Map<Long, Question> questionEntityMap = buildEntityMap(questionEntities, Question::getId);

        for (QuestionResponse questionResp : questionResponses) {
            if (questionResp.getId() == null) continue;
            
            Question questionEntity = questionEntityMap.get(questionResp.getId());
            Set<Long> selectedAnswerIds = userAnswerMap.getOrDefault(questionResp.getId(), Collections.emptySet());
            
            List<Answer> answerEntities = questionEntity != null ? questionEntity.getAnswers() : null;
            applyAnswerListVisibility(questionResp.getAnswers(), answerEntities, canSee, selectedAnswerIds);
        }
    }

    private static void applyAnswerListVisibility(List<AnswerResponse> answerResponses, List<Answer> answerEntities, boolean canSee, Set<Long> selectedAnswerIds) {
        if (answerResponses == null) return;

        Map<Long, Answer> answerEntityMap = buildEntityMap(answerEntities, Answer::getId);

        for (AnswerResponse answerResp : answerResponses) {
            if (answerResp.getId() == null) continue;
            
            Answer answerEntity = answerEntityMap.get(answerResp.getId());
            boolean isCorrect = answerEntity != null && answerEntity.isCorrect();
            answerResp.setCorrect(canSee && isCorrect);
            answerResp.setSelected(selectedAnswerIds.contains(answerResp.getId()));
        }
    }

    private static <T> Map<Long, T> buildEntityMap(List<T> entities, Function<T, Long> idExtractor) {
        if (entities == null) return Collections.emptyMap();
        return entities.stream()
                .filter(e -> idExtractor.apply(e) != null)
                .collect(Collectors.toMap(idExtractor, Function.identity()));
    }
}
