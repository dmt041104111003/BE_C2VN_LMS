package com.cardano_lms.server.Service;

import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Repository.EnrolledSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final EnrolledSnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public EnrolledSnapshot createSnapshot(Enrollment enrollment, Course course) {
        if (snapshotRepository.existsByEnrollmentId(enrollment.getId())) {
            return snapshotRepository.findByEnrollmentId(enrollment.getId()).orElse(null);
        }

        EnrolledSnapshot snapshot = EnrolledSnapshot.builder()
                .enrollment(enrollment)
                .originalCourseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseImageUrl(course.getImageUrl())
                .courseVideoUrl(course.getVideoUrl())
                .instructorName(getInstructorName(course))
                .instructorId(getInstructorId(course))
                .structureJson(buildStructureJson(course))
                .courseVersionAt(getCourseVersion(course))
                .version(1)
                .createdAt(LocalDateTime.now())
                .build();

        return snapshotRepository.save(snapshot);
    }

    private String buildStructureJson(Course course) {
        Map<String, Object> structure = new HashMap<>();
        AtomicInteger chapterIdGen = new AtomicInteger(1);
        AtomicInteger lectureIdGen = new AtomicInteger(1);
        AtomicInteger testIdGen = new AtomicInteger(1);

        structure.put("chapters", buildChaptersData(course, chapterIdGen, lectureIdGen, testIdGen));
        structure.put("courseTests", buildCourseTestsData(course, testIdGen));

        try {
            return objectMapper.writeValueAsString(structure);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private List<Map<String, Object>> buildChaptersData(Course course, AtomicInteger chapterIdGen, 
            AtomicInteger lectureIdGen, AtomicInteger testIdGen) {
        if (course.getChapters() == null) return Collections.emptyList();

        List<Map<String, Object>> chapters = new ArrayList<>();
        for (Chapter chapter : course.getChapters()) {
            Map<String, Object> chapterData = new HashMap<>();
            chapterData.put("id", chapterIdGen.getAndIncrement());
            chapterData.put("originalId", chapter.getId());
            chapterData.put("title", chapter.getTitle());
            chapterData.put("orderIndex", chapter.getOrderIndex());
            chapterData.put("lectures", buildLecturesData(chapter, lectureIdGen));
            chapterData.put("tests", buildTestsData(chapter.getTests(), testIdGen));
            chapters.add(chapterData);
        }
        return chapters;
    }

    private List<Map<String, Object>> buildLecturesData(Chapter chapter, AtomicInteger lectureIdGen) {
        if (chapter.getLectures() == null) return Collections.emptyList();

        List<Map<String, Object>> lectures = new ArrayList<>();
        for (Lecture lecture : chapter.getLectures()) {
            Map<String, Object> lectureData = new HashMap<>();
            lectureData.put("id", lectureIdGen.getAndIncrement());
            lectureData.put("originalId", lecture.getId());
            lectureData.put("title", lecture.getTitle());
            lectureData.put("description", lecture.getDescription());
            lectureData.put("videoUrl", lecture.getVideoUrl());
            lectureData.put("duration", lecture.getTime());
            lectureData.put("orderIndex", lecture.getOrderIndex());
            lectures.add(lectureData);
        }
        return lectures;
    }

    private List<Map<String, Object>> buildTestsData(List<Test> tests, AtomicInteger testIdGen) {
        if (tests == null) return Collections.emptyList();

        List<Map<String, Object>> testsList = new ArrayList<>();
        for (Test test : tests) {
            testsList.add(buildTestData(test, testIdGen));
        }
        return testsList;
    }

    private List<Map<String, Object>> buildCourseTestsData(Course course, AtomicInteger testIdGen) {
        return buildTestsData(course.getCourseTests(), testIdGen);
    }

    private Map<String, Object> buildTestData(Test test, AtomicInteger testIdGen) {
        Map<String, Object> testData = new HashMap<>();
        testData.put("id", testIdGen.getAndIncrement());
        testData.put("originalId", test.getId());
        testData.put("title", test.getTitle());
        testData.put("durationMinutes", test.getDurationMinutes());
        testData.put("passScore", test.getPassScore());
        testData.put("questions", buildQuestionsData(test.getQuestions()));
        return testData;
    }

    private List<Map<String, Object>> buildQuestionsData(List<Question> questions) {
        if (questions == null) return Collections.emptyList();

        List<Map<String, Object>> questionsList = new ArrayList<>();
        for (Question question : questions) {
            Map<String, Object> qData = new HashMap<>();
            qData.put("id", question.getId());
            qData.put("content", question.getContent());
            qData.put("explanation", question.getExplanation());
            qData.put("orderIndex", question.getOrderIndex());
            qData.put("score", question.getScore());
            qData.put("answers", buildAnswersData(question.getAnswers()));
            questionsList.add(qData);
        }
        return questionsList;
    }

    private List<Map<String, Object>> buildAnswersData(List<Answer> answers) {
        if (answers == null) return Collections.emptyList();

        List<Map<String, Object>> answersList = new ArrayList<>();
        for (Answer answer : answers) {
            Map<String, Object> aData = new HashMap<>();
            aData.put("id", answer.getId());
            aData.put("content", answer.getContent());
            aData.put("correct", answer.isCorrect());
            answersList.add(aData);
        }
        return answersList;
    }

    public Optional<EnrolledSnapshot> getByEnrollmentId(Long enrollmentId) {
        return snapshotRepository.findByEnrollmentId(enrollmentId);
    }

    public Optional<EnrolledSnapshot> getByUserAndCourse(String userId, String courseId) {
        return snapshotRepository.findByUserIdAndCourseId(userId, courseId);
    }

    public boolean hasNewVersion(EnrolledSnapshot snapshot, Course course) {
        if (snapshot == null || course == null) return false;
        if (snapshot.getCourseVersionAt() == null || course.getUpdatedAt() == null) return false;
        return course.getUpdatedAt().isAfter(snapshot.getCourseVersionAt());
    }

    @Transactional
    public EnrolledSnapshot upgradeSnapshot(EnrolledSnapshot snapshot, Course course) {
        snapshot.setCourseTitle(course.getTitle());
        snapshot.setCourseDescription(course.getDescription());
        snapshot.setCourseImageUrl(course.getImageUrl());
        snapshot.setCourseVideoUrl(course.getVideoUrl());
        snapshot.setInstructorName(getInstructorName(course));
        snapshot.setInstructorId(getInstructorId(course));
        snapshot.setStructureJson(buildStructureJson(course));
        snapshot.setCourseVersionAt(getCourseVersion(course));
        snapshot.setVersion(snapshot.getVersion() + 1);
        snapshot.setUpgradedAt(LocalDateTime.now());

        return snapshotRepository.save(snapshot);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parseStructure(String structureJson) {
        try {
            return objectMapper.readValue(structureJson, Map.class);
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private String getInstructorName(Course course) {
        return course.getInstructor() != null ? course.getInstructor().getFullName() : null;
    }

    private String getInstructorId(Course course) {
        return course.getInstructor() != null ? course.getInstructor().getId() : null;
    }

    private LocalDateTime getCourseVersion(Course course) {
        return course.getUpdatedAt() != null ? course.getUpdatedAt() : LocalDateTime.now();
    }
}
