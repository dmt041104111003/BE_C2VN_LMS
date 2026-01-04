package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Request.ProgressCreationRequest;
import com.cardano_lms.server.DTO.Request.TestSubmissionRequest;
import com.cardano_lms.server.DTO.Response.QuestionResultResponse;
import com.cardano_lms.server.DTO.Response.TestResultResponse;
import com.cardano_lms.server.DTO.TestResult;
import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.*;
import com.cardano_lms.server.constant.CourseContentType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseTestService {

    CourseRepository courseRepository;
    TestRepository testRepository;
    UserRepository userRepository;
    UserAnswerRepository userAnswerRepository;
    AnswerRepository answerRepository;
    EnrollmentRepository enrollmentRepository;
    EnrolledSnapshotRepository snapshotRepository;
    ProgressService progressService;
    SnapshotService snapshotService;

    @Transactional
    public TestResultResponse evaluateTest(TestSubmissionRequest submission, Long testId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        User user = userRepository.findById(submission.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return testRepository.findById(testId)
                .map(test -> evaluateFromDb(submission, test, course, user))
                .orElseGet(() -> evaluateFromSnapshot(submission, testId, course, user));
    }

    public TestResultResponse getPreviousTestResult(String userId, Long testId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Enrollment enrollment = enrollmentRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.HAVE_NOT_JOIN_THIS_COURSE));

        Progress progress = progressService.getProgressByEnrollment(enrollment.getId());
        if (progress == null || !progress.isTestCompleted(testId)) {
            return null;
        }

        TestResult testResult = progress.getTestResult(testId);
        if (testResult == null || !Boolean.TRUE.equals(testResult.getPassed())) {
            return null;
        }

        return testRepository.findById(testId)
                .map(test -> getPreviousFromDb(userId, test, courseId, testResult))
                .orElseGet(() -> getPreviousFromSnapshot(userId, testId, course, testResult));
    }

    private TestResultResponse evaluateFromDb(TestSubmissionRequest submission, Test test, Course course, User user) {
        List<Question> questions = test.getQuestions();
        Map<Long, List<Long>> submittedAnswers = buildSubmittedAnswersMap(submission, questions);
        
        EvaluationResult eval = evaluateQuestions(questions, submittedAnswers);
        
        persistUserAnswers(user, test, questions, submittedAnswers);

        double scorePercent = calculateScorePercent(eval.totalScore, eval.maxScore);
        boolean passed = scorePercent >= test.getPassScore();

        if (passed) {
            createTestProgress(test.getId(), (int) scorePercent, user.getId(), course.getId());
        }

        return buildTestResultResponse(test.getId(), user.getId(), course.getId(), questions.size(),
                eval.correctCount, eval.maxScore, scorePercent, test.getPassScore(), passed, eval.results);
    }

    @SuppressWarnings("unchecked")
    private TestResultResponse evaluateFromSnapshot(TestSubmissionRequest submission, Long testId, Course course, User user) {
        EnrolledSnapshot snapshot = snapshotRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        Map<String, Object> structure = snapshotService.parseStructure(snapshot.getStructureJson());
        Map<String, Object> testData = findTestInSnapshot(structure, testId);
        
        if (testData == null) {
            throw new AppException(ErrorCode.TEST_NOT_FOUND);
        }

        String testTitle = (String) testData.get("title");
        int passScore = getIntValue(testData, "passScore", 0);
        List<Map<String, Object>> questions = (List<Map<String, Object>>) testData.getOrDefault("questions", Collections.emptyList());

        EvaluationResult eval = evaluateSnapshotQuestions(submission, questions, user, testTitle);

        double scorePercent = calculateScorePercent(eval.totalScore, eval.maxScore);
        boolean passed = scorePercent >= passScore;

        if (passed) {
            createTestProgress(testId, (int) scorePercent, user.getId(), course.getId());
        }

        return buildTestResultResponse(testId, user.getId(), course.getId(), questions.size(),
                eval.correctCount, eval.maxScore, scorePercent, passScore, passed, eval.results);
    }

    private TestResultResponse getPreviousFromDb(String userId, Test test, String courseId, TestResult testResult) {
        List<UserAnswer> userAnswers = userAnswerRepository.findByUserIdAndTestId(userId, test.getId());
        Map<Long, List<Long>> userAnswerMap = buildUserAnswerMap(userAnswers);

        List<Question> questions = test.getQuestions();
        EvaluationResult eval = evaluateQuestions(questions, userAnswerMap);

        return buildTestResultResponse(test.getId(), userId, courseId, questions.size(),
                eval.correctCount, eval.maxScore, testResult.getScore(), test.getPassScore(), true, eval.results);
    }

    @SuppressWarnings("unchecked")
    private TestResultResponse getPreviousFromSnapshot(String userId, Long testId, Course course, TestResult testResult) {
        EnrolledSnapshot snapshot = snapshotRepository.findByUserIdAndCourseId(userId, course.getId()).orElse(null);
        if (snapshot == null) return null;

        Map<String, Object> structure = snapshotService.parseStructure(snapshot.getStructureJson());
        Map<String, Object> testData = findTestInSnapshot(structure, testId);
        if (testData == null) return null;

        String testTitle = (String) testData.get("title");
        int passScore = getIntValue(testData, "passScore", 0);
        List<Map<String, Object>> questions = (List<Map<String, Object>>) testData.getOrDefault("questions", Collections.emptyList());

        List<UserAnswer> userAnswers = userAnswerRepository.findByUserIdAndTestTitleSnapshot(userId, testTitle);
        Map<String, List<String>> userAnswerContentMap = buildUserAnswerContentMap(userAnswers);

        EvaluationResult eval = evaluateSnapshotQuestionsFromHistory(questions, userAnswerContentMap);

        return buildTestResultResponse(testId, userId, course.getId(), questions.size(),
                eval.correctCount, eval.maxScore, testResult.getScore(), passScore, true, eval.results);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findTestInSnapshot(Map<String, Object> structure, Long testId) {
        List<Map<String, Object>> courseTests = (List<Map<String, Object>>) structure.getOrDefault("courseTests", Collections.emptyList());
        for (Map<String, Object> test : courseTests) {
            if (testId.equals(((Number) test.get("originalId")).longValue())) {
                return test;
            }
        }

        List<Map<String, Object>> chapters = (List<Map<String, Object>>) structure.getOrDefault("chapters", Collections.emptyList());
        for (Map<String, Object> chapter : chapters) {
            List<Map<String, Object>> tests = (List<Map<String, Object>>) chapter.getOrDefault("tests", Collections.emptyList());
            for (Map<String, Object> test : tests) {
                if (testId.equals(((Number) test.get("originalId")).longValue())) {
                    return test;
                }
            }
        }
        return null;
    }

    private Map<Long, List<Long>> buildSubmittedAnswersMap(TestSubmissionRequest submission, List<Question> questions) {
        return submission.getAnswers().stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuestionId(),
                        Collectors.flatMapping(a -> a.getAnswerId().stream(), Collectors.toList())
                ));
    }

    private Map<Long, List<Long>> buildUserAnswerMap(List<UserAnswer> userAnswers) {
        return userAnswers.stream()
                .filter(ua -> ua.getQuestion() != null && ua.getAnswer() != null)
                .collect(Collectors.groupingBy(
                        ua -> ua.getQuestion().getId(),
                        Collectors.mapping(ua -> ua.getAnswer().getId(), Collectors.toList())
                ));
    }

    private Map<String, List<String>> buildUserAnswerContentMap(List<UserAnswer> userAnswers) {
        return userAnswers.stream()
                .filter(ua -> ua.getQuestionContentSnapshot() != null)
                .collect(Collectors.groupingBy(
                        UserAnswer::getQuestionContentSnapshot,
                        Collectors.mapping(UserAnswer::getAnswerContentSnapshot, Collectors.toList())
                ));
    }

    private EvaluationResult evaluateQuestions(List<Question> questions, Map<Long, List<Long>> submittedAnswers) {
        int totalScore = 0, maxScore = 0, correctCount = 0;
        List<QuestionResultResponse> results = new ArrayList<>();

        for (Question q : questions) {
            maxScore += q.getScore();
            List<Long> selected = submittedAnswers.getOrDefault(q.getId(), List.of());
            Set<Long> correctIds = q.getAnswers().stream()
                    .filter(Answer::isCorrect)
                    .map(Answer::getId)
                    .collect(Collectors.toSet());

            boolean isCorrect = new HashSet<>(selected).equals(correctIds);
            if (isCorrect) {
                totalScore += q.getScore();
                correctCount++;
            }

            results.add(QuestionResultResponse.builder()
                    .questionId(q.getId())
                    .correctAnswerIds(new ArrayList<>(correctIds))
                    .selectedAnswerIds(selected)
                    .isCorrect(isCorrect)
                    .explanation(q.getExplanation())
                    .build());
        }

        return new EvaluationResult(totalScore, maxScore, correctCount, results);
    }

    @SuppressWarnings("unchecked")
    private EvaluationResult evaluateSnapshotQuestions(TestSubmissionRequest submission, 
            List<Map<String, Object>> questions, User user, String testTitle) {
        int totalScore = 0, maxScore = 0, correctCount = 0;
        List<QuestionResultResponse> results = new ArrayList<>();

        for (Map<String, Object> qData : questions) {
            Long questionId = ((Number) qData.get("id")).longValue();
            String questionContent = (String) qData.get("content");
            int qScore = getIntValue(qData, "score", 1);
            maxScore += qScore;

            List<Map<String, Object>> answersData = (List<Map<String, Object>>) qData.getOrDefault("answers", Collections.emptyList());

            List<Long> selected = submission.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(questionId))
                    .flatMap(a -> a.getAnswerId().stream())
                    .toList();

            Set<Long> correctIds = answersData.stream()
                    .filter(a -> Boolean.TRUE.equals(a.get("correct")))
                    .map(a -> ((Number) a.get("id")).longValue())
                    .collect(Collectors.toSet());

            persistSnapshotUserAnswers(user, testTitle, questionContent, selected, answersData);

            boolean isCorrect = new HashSet<>(selected).equals(correctIds);
            if (isCorrect) {
                totalScore += qScore;
                correctCount++;
            }

            results.add(QuestionResultResponse.builder()
                    .questionId(questionId)
                    .correctAnswerIds(new ArrayList<>(correctIds))
                    .selectedAnswerIds(selected)
                    .isCorrect(isCorrect)
                    .explanation((String) qData.get("explanation"))
                    .build());
        }

        return new EvaluationResult(totalScore, maxScore, correctCount, results);
    }

    @SuppressWarnings("unchecked")
    private EvaluationResult evaluateSnapshotQuestionsFromHistory(List<Map<String, Object>> questions,
            Map<String, List<String>> userAnswerContentMap) {
        int totalScore = 0, maxScore = 0, correctCount = 0;
        List<QuestionResultResponse> results = new ArrayList<>();

        for (Map<String, Object> qData : questions) {
            Long questionId = ((Number) qData.get("id")).longValue();
            String questionContent = (String) qData.get("content");
            int qScore = getIntValue(qData, "score", 1);
            maxScore += qScore;

            List<Map<String, Object>> answersData = (List<Map<String, Object>>) qData.getOrDefault("answers", Collections.emptyList());
            List<String> userSelectedContents = userAnswerContentMap.getOrDefault(questionContent, List.of());

            List<Long> selected = answersData.stream()
                    .filter(a -> userSelectedContents.contains(a.get("content")))
                    .map(a -> ((Number) a.get("id")).longValue())
                    .toList();

            Set<Long> correctIds = answersData.stream()
                    .filter(a -> Boolean.TRUE.equals(a.get("correct")))
                    .map(a -> ((Number) a.get("id")).longValue())
                    .collect(Collectors.toSet());

            boolean isCorrect = new HashSet<>(selected).equals(correctIds);
            if (isCorrect) {
                totalScore += qScore;
                correctCount++;
            }

            results.add(QuestionResultResponse.builder()
                    .questionId(questionId)
                    .correctAnswerIds(new ArrayList<>(correctIds))
                    .selectedAnswerIds(selected)
                    .isCorrect(isCorrect)
                    .explanation((String) qData.get("explanation"))
                    .build());
        }

        return new EvaluationResult(totalScore, maxScore, correctCount, results);
    }

    private void persistUserAnswers(User user, Test test, List<Question> questions, Map<Long, List<Long>> submittedAnswers) {
        for (Question q : questions) {
            List<Long> answerIds = submittedAnswers.getOrDefault(q.getId(), List.of());
            for (Long answerId : answerIds) {
                Answer answer = answerRepository.findById(answerId).orElse(null);
                UserAnswer ua = UserAnswer.builder()
                        .user(user)
                        .test(test)
                        .question(q)
                        .answer(answer)
                        .testTitleSnapshot(test.getTitle())
                        .questionContentSnapshot(q.getContent())
                        .answerContentSnapshot(answer != null ? answer.getContent() : null)
                        .isCorrectSnapshot(answer != null && answer.isCorrect())
                        .build();
                userAnswerRepository.save(ua);
            }
        }
    }

    private void persistSnapshotUserAnswers(User user, String testTitle, String questionContent,
            List<Long> selectedIds, List<Map<String, Object>> answersData) {
        for (Long answerId : selectedIds) {
            Map<String, Object> answerData = answersData.stream()
                    .filter(a -> ((Number) a.get("id")).longValue() == answerId)
                    .findFirst().orElse(null);

            UserAnswer ua = UserAnswer.builder()
                    .user(user)
                    .testTitleSnapshot(testTitle)
                    .questionContentSnapshot(questionContent)
                    .answerContentSnapshot(answerData != null ? (String) answerData.get("content") : null)
                    .isCorrectSnapshot(answerData != null && Boolean.TRUE.equals(answerData.get("correct")))
                    .build();
            userAnswerRepository.save(ua);
        }
    }

    private void createTestProgress(Long testId, int score, String userId, String courseId) {
        progressService.createProgress(
                ProgressCreationRequest.builder()
                        .testId(testId)
                        .score(score)
                        .type(CourseContentType.TEST)
                        .build(),
                userId, courseId);
    }

    private double calculateScorePercent(int totalScore, int maxScore) {
        return maxScore > 0 ? ((double) totalScore / maxScore) * 100 : 0;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        return value != null ? ((Number) value).intValue() : defaultValue;
    }

    private TestResultResponse buildTestResultResponse(Long testId, String userId, String courseId,
            int totalQuestions, int correctAnswers, int maxScore, double score,
            int passScore, boolean passed, List<QuestionResultResponse> results) {
        return TestResultResponse.builder()
                .testId(testId)
                .userId(userId)
                .courseId(courseId)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .maxScore(maxScore)
                .score(score)
                .passScore(passScore)
                .passed(passed)
                .questionResults(results)
                .build();
    }

    private record EvaluationResult(int totalScore, int maxScore, int correctCount, List<QuestionResultResponse> results) {}
}

