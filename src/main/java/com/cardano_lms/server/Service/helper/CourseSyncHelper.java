package com.cardano_lms.server.Service.helper;

import com.cardano_lms.server.DTO.Request.*;
import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Mapper.*;
import com.cardano_lms.server.Repository.LectureCommentReactionRepository;
import com.cardano_lms.server.Repository.LectureCommentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseSyncHelper {

    TestMapper testMapper;
    ChapterMapper chapterMapper;
    QuestionMapper questionMapper;
    AnswerMapper answerMapper;
    LectureMapper lectureMapper;
    LectureCommentRepository lectureCommentRepository;
    LectureCommentReactionRepository lectureCommentReactionRepository;

    public void syncCourseTests(Course course, List<TestRequest> testRequests) {
        if (testRequests == null) return;

        Map<Long, Test> existingMap = toIdMap(course.getCourseTests(), Test::getId);
        Set<Long> requestIds = extractIds(testRequests, TestRequest::getId);

        course.getCourseTests().removeIf(t -> t.getId() != null && !requestIds.contains(t.getId()));

        for (TestRequest req : testRequests) {
            if (req.getId() != null && existingMap.containsKey(req.getId())) {
                Test existing = existingMap.get(req.getId());
                detachTestFromChapters(course, existing);
                existing.setChapter(null);
                existing.setCourse(course);
                testMapper.updateTest(req, existing);
                rebuildQuestions(existing, req.getQuestions());
            } else {
                course.getCourseTests().add(buildTest(req, course, null));
            }
        }
    }

    public void syncChapters(Course course, List<ChapterRequest> chapterRequests) {
        if (chapterRequests == null) return;

        Map<Long, Chapter> existingMap = toIdMap(course.getChapters(), Chapter::getId);
        Set<Long> requestIds = extractIds(chapterRequests, ChapterRequest::getId);

        course.getChapters().removeIf(c -> c.getId() != null && !requestIds.contains(c.getId()));

        for (ChapterRequest req : chapterRequests) {
            if (req.getId() != null && existingMap.containsKey(req.getId())) {
                Chapter existing = existingMap.get(req.getId());
                chapterMapper.updateChapter(req, existing);
                if (req.getTests() != null) syncChapterTests(course, existing, req.getTests());
                if (req.getLectures() != null) syncLectures(existing, req.getLectures());
            } else {
                course.getChapters().add(buildChapter(req, course));
            }
        }
    }

    public void syncChapterTests(Course course, Chapter chapter, List<TestRequest> testRequests) {
        if (testRequests == null || testRequests.isEmpty()) return;

        Map<Long, Test> existingMap = toIdMap(chapter.getTests(), Test::getId);
        Set<Long> requestIds = extractIds(testRequests, TestRequest::getId);

        chapter.getTests().removeIf(t -> t.getId() != null && !requestIds.contains(t.getId()));

        for (TestRequest req : testRequests) {
            if (req.getId() != null && existingMap.containsKey(req.getId())) {
                Test existing = existingMap.get(req.getId());
                course.getCourseTests().removeIf(t -> t.getId() != null && t.getId().equals(existing.getId()));
                existing.setCourse(null);
                existing.setChapter(chapter);
                testMapper.updateTest(req, existing);
                rebuildQuestions(existing, req.getQuestions());
            } else {
                chapter.getTests().add(buildTest(req, null, chapter));
            }
        }
    }

    public void syncLectures(Chapter chapter, List<LectureRequest> lectureRequests) {
        if (lectureRequests == null || lectureRequests.isEmpty()) return;

        Map<Long, Lecture> existingMap = toIdMap(chapter.getLectures(), Lecture::getId);
        Set<Long> requestIds = extractIds(lectureRequests, LectureRequest::getId);

        deleteLectureCommentsForRemoved(chapter, requestIds);
        chapter.getLectures().removeIf(l -> l.getId() != null && !requestIds.contains(l.getId()));

        for (LectureRequest req : lectureRequests) {
            if (req.getId() != null && existingMap.containsKey(req.getId())) {
                lectureMapper.updateLecture(req, existingMap.get(req.getId()));
            } else {
                chapter.getLectures().add(buildLecture(req, chapter));
            }
        }
    }

    public Test buildTestWithQuestions(TestRequest testReq, Course course, Chapter chapter) {
        Test test = testMapper.toEntity(testReq);
        if (course != null) course.addTest(test);
        if (chapter != null) chapter.addTest(test);

        if (testReq.getQuestions() != null) {
            testReq.getQuestions().forEach(qReq -> {
                Question question = questionMapper.toQuestion(qReq);
                test.addQuestion(question);
                if (qReq.getAnswers() != null) {
                    qReq.getAnswers().forEach(aReq -> question.addAnswer(answerMapper.toAnswer(aReq)));
                }
            });
        }
        return test;
    }

    public Chapter buildChapter(ChapterRequest req, Course course) {
        Chapter chapter = chapterMapper.toEntity(req);
        chapter.setId(null);
        chapter.setCourse(course);
        chapter.setLectures(new ArrayList<>());
        chapter.setTests(new ArrayList<>());

        if (req.getTests() != null) {
            req.getTests().forEach(testReq -> chapter.getTests().add(buildTest(testReq, null, chapter)));
        }
        if (req.getLectures() != null) {
            req.getLectures().forEach(lecReq -> chapter.getLectures().add(buildLecture(lecReq, chapter)));
        }
        return chapter;
    }

    private Test buildTest(TestRequest req, Course course, Chapter chapter) {
        Test test = testMapper.toEntity(req);
        test.setId(null);
        test.setCourse(course);
        test.setChapter(chapter);
        test.setCreatedAt(LocalDateTime.now());
        test.setQuestions(new ArrayList<>());

        if (req.getQuestions() != null) {
            req.getQuestions().forEach(qReq -> {
                Question q = buildQuestion(qReq, test);
                test.getQuestions().add(q);
            });
        }
        return test;
    }

    private Question buildQuestion(QuestionRequest req, Test test) {
        Question question = questionMapper.toQuestion(req);
        question.setId(null);
        question.setTest(test);
        question.setAnswers(new ArrayList<>());

        if (req.getAnswers() != null) {
            req.getAnswers().forEach(aReq -> {
                Answer answer = answerMapper.toAnswer(aReq);
                answer.setId(null);
                question.addAnswer(answer);
            });
        }
        return question;
    }

    private Lecture buildLecture(LectureRequest req, Chapter chapter) {
        Lecture lecture = lectureMapper.toEntity(req);
        lecture.setId(null);
        lecture.setChapter(chapter);
        lecture.setCreatedAt(LocalDateTime.now());
        return lecture;
    }

    private void rebuildQuestions(Test test, List<QuestionRequest> questionRequests) {
        test.getQuestions().clear();
        if (questionRequests == null) return;

        for (QuestionRequest qReq : questionRequests) {
            Question q = buildQuestion(qReq, test);
            test.addQuestion(q);
        }
    }

    private void detachTestFromChapters(Course course, Test test) {
        for (Chapter chapter : course.getChapters()) {
            chapter.getTests().removeIf(t -> t.getId() != null && t.getId().equals(test.getId()));
        }
    }

    private void deleteLectureCommentsForRemoved(Chapter chapter, Set<Long> validIds) {
        for (Lecture lecture : chapter.getLectures()) {
            if (lecture.getId() != null && !validIds.contains(lecture.getId())) {
                List<Long> commentIds = lectureCommentRepository.findIdsByLectureId(lecture.getId());
                if (!commentIds.isEmpty()) {
                    lectureCommentReactionRepository.deleteByCommentIdIn(commentIds);
                    lectureCommentRepository.deleteByLectureId(lecture.getId());
                }
            }
        }
    }

    private <T, R> Map<R, T> toIdMap(List<T> entities, Function<T, R> idExtractor) {
        if (entities == null) return Collections.emptyMap();
        return entities.stream()
                .filter(e -> idExtractor.apply(e) != null)
                .collect(Collectors.toMap(idExtractor, Function.identity()));
    }

    private <T, R> Set<R> extractIds(List<T> requests, Function<T, R> idExtractor) {
        if (requests == null) return Collections.emptySet();
        return requests.stream()
                .filter(r -> idExtractor.apply(r) != null)
                .map(idExtractor)
                .collect(Collectors.toSet());
    }
}

