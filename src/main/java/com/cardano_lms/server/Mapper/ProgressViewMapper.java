package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Response.*;
import com.cardano_lms.server.DTO.TestResult;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.Enrollment;
import com.cardano_lms.server.Entity.Progress;
import com.cardano_lms.server.Repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProgressViewMapper {

    private final ProgressRepository progressRepository;

    public ProgressResponse toProgressResponse(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        
        
        Progress progress = progressRepository.findByEnrollment_Id(enrollment.getId()).orElse(null);
        
        Set<Long> completedLectureIds = progress != null 
            ? progress.getCompletedLectureIdSet() 
            : Set.of();
        List<TestResult> completedTests = progress != null 
            ? progress.getCompletedTests() 
            : List.of();

        List<ProgressChapterResponse> chapterResponses = course.getChapters().stream()
            .map(chapter -> ProgressChapterResponse.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .orderIndex(chapter.getOrderIndex())
                .lectures(chapter.getLectures().stream()
                    .map(lecture -> ProgressLectureResponse.builder()
                        .id(lecture.getId())
                        .title(lecture.getTitle())
                        .orderIndex(lecture.getOrderIndex())
                        .duration(lecture.getTime())
                        .build())
                    .toList())
                .tests(chapter.getTests().stream()
                    .map(test -> ProgressTestResponse.builder()
                        .id(test.getId())
                        .title(test.getTitle())
                        .build())
                    .toList())
                .build())
            .toList();

        List<ProgressTestResponse> testResponses = course.getCourseTests().stream()
            .map(test -> ProgressTestResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .orderIndex(test.getOrderIndex())
                .build())
            .toList();

        
        List<TestAndLectureCompletedResponse> progressResponses = new ArrayList<>();
        
        
        for (Long lectureId : completedLectureIds) {
            progressResponses.add(TestAndLectureCompletedResponse.builder()
                .type("lecture")
                .completed(true)
                .contentId(lectureId)
                .build());
        }
        
        
        for (TestResult testResult : completedTests) {
            progressResponses.add(TestAndLectureCompletedResponse.builder()
                .type("test")
                .completed(Boolean.TRUE.equals(testResult.getPassed()))
                .contentId(testResult.getTestId())
                .score(testResult.getScore())
                .attempts(testResult.getAttempts())
                .completedAt(testResult.getCompletedAt())
                .build());
        }

        return ProgressResponse.builder()
            .id(course.getId())
            .title(course.getTitle())
            .imageUrl(course.getImageUrl())
            .completed(enrollment.isCompleted())
            .instructorName(course.getInstructor().getFullName())
            .fullName(enrollment.getUser() != null ? enrollment.getUser().getFullName() : null)
            .certificateId(enrollment.getCertificate() != null ? enrollment.getCertificate().getId() : null)
            .chapters(chapterResponses)
            .courseTests(testResponses)
            .testAndLectureCompleted(progressResponses)
            .build();
    }
}
