package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.constant.CourseContentType;
import com.cardano_lms.server.DTO.Request.ProgressCreationRequest;
import com.cardano_lms.server.DTO.Response.*;
import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.ProgressViewMapper;
import com.cardano_lms.server.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final LectureRepository lectureRepository;
    private final ProgressRepository progressRepository;
    private final ProgressViewMapper progressViewMapper;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ProgressResponse> getUserProgress(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        
        return enrollmentRepository.findAllByUser_Id(userId).stream()
            .map(progressViewMapper::toProgressResponse)
            .toList();
    }

    @Transactional
    public Progress createProgress(ProgressCreationRequest request, String userId, String courseId) {
        if (userId == null || courseId == null) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
        
        Enrollment enrollment = enrollmentRepository.findByUser_IdAndCourse_Id(userId, courseId)
            .orElseThrow(() -> new AppException(ErrorCode.HAVE_NOT_JOIN_THIS_COURSE));
        
        Progress progress = getOrCreateProgress(enrollment);
        
        if (request.getType() == CourseContentType.TEST) {
            handleTestProgress(request, progress);
        } else {
            handleLectureProgress(request, progress);
        }
        
        progress.setLastAccessedAt(LocalDateTime.now());
        
        updateCourseCompletionStatus(progress, enrollment.getCourse());
        
        return progressRepository.save(progress);
    }
    
    private void updateCourseCompletionStatus(Progress progress, Course course) {
        int totalLectures = course.getChapters().stream()
                .mapToInt(ch -> ch.getLectures() != null ? ch.getLectures().size() : 0)
                .sum();
        int totalTests = course.getChapters().stream()
                .mapToInt(ch -> ch.getTests() != null ? ch.getTests().size() : 0)
                .sum() + (course.getCourseTests() != null ? course.getCourseTests().size() : 0);
        
        int lecturesCompleted = progress.getCompletedLectureIdSet().size();
        int testsCompleted = (int) progress.getCompletedTests().stream()
                .filter(t -> Boolean.TRUE.equals(t.getPassed()))
                .count();
        
        boolean allLecturesCompleted = totalLectures == 0 || lecturesCompleted >= totalLectures;
        boolean allTestsCompleted = totalTests == 0 || testsCompleted >= totalTests;
        boolean courseCompleted = allLecturesCompleted && allTestsCompleted;
        
        progress.setCourseCompleted(courseCompleted);
        
        Enrollment enrollment = progress.getEnrollment();
        if (courseCompleted && !enrollment.isCompleted()) {
            enrollment.setCompleted(true);
            enrollmentRepository.save(enrollment);
        }
    }

    private Progress getOrCreateProgress(Enrollment enrollment) {
        return progressRepository.findByEnrollment_Id(enrollment.getId())
            .orElseGet(() -> {
                Progress newProgress = new Progress();
                newProgress.setEnrollment(enrollment);
                newProgress.setCompletedLectureIds("");
                newProgress.setCompletedTestsJson("[]");
                newProgress.setCourseCompleted(false);
                newProgress.setLastAccessedAt(LocalDateTime.now());
                return newProgress;
            });
    }

    private void handleTestProgress(ProgressCreationRequest request, Progress progress) {
        if (request.getTestId() == null) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
        
        Long testId = request.getTestId();
        int score = request.getScore() != null ? request.getScore() : 0;
        
        
        
        int passScore;
        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isPresent()) {
            passScore = testOpt.get().getPassScore();
        } else {
            passScore = 60;
        }
        
        boolean passed = score >= passScore;
        
        if (progress.isTestCompleted(testId)) {
            return;
        }
        
        progress.addOrUpdateTestResult(testId, score, passed);
    }

    private void handleLectureProgress(ProgressCreationRequest request, Progress progress) {
        if (request.getLectureId() == null) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
        
        Long lectureId = request.getLectureId();
        
        if (progress.isLectureCompleted(lectureId)) {
            return;
        }
        
        progress.addCompletedLecture(lectureId);
    }

    public ApiResponse<List<ActivityResponse>> getUserActivity(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findAllByUser_Id(userId);
        
        List<ActivityResponse> activities = enrollments.stream()
            .map(enrollment -> progressRepository.findByEnrollment_Id(enrollment.getId()).orElse(null))
            .filter(progress -> progress != null)
            .flatMap(progress -> {
                var lectureActivities = progress.getCompletedLectureIdSet().stream()
                    .map(lectureId -> ActivityResponse.builder()
                        .type("lecture")
                        .progressId(progress.getId())
                        .completedAt(null)
                        .build());
                
                var testActivities = progress.getCompletedTests().stream()
                    .filter(test -> Boolean.TRUE.equals(test.getPassed()))
                    .map(test -> ActivityResponse.builder()
                        .type("test")
                        .progressId(progress.getId())
                        .completedAt(test.getCompletedAt())
                        .build());
                
                return java.util.stream.Stream.concat(lectureActivities, testActivities);
            })
            .toList();
        
        return ApiResponse.<List<ActivityResponse>>builder()
            .result(activities)
            .build();
    }
    
    @Transactional
    public void resetProgressForEnrollment(Long enrollmentId) {
        progressRepository.deleteByEnrollment_Id(enrollmentId);
    }
    
    public Progress getProgressByEnrollment(Long enrollmentId) {
        return progressRepository.findByEnrollment_Id(enrollmentId).orElse(null);
    }
}
