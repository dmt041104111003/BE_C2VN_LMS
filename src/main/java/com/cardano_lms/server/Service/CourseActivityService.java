package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Response.CourseActivityResponse;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.CourseActivity;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Repository.CourseActivityRepository;
import com.cardano_lms.server.Repository.CourseRepository;
import com.cardano_lms.server.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseActivityService {
    private final CourseActivityRepository activityRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public static final String ACTIVITY_COURSE_CREATED = "course_created";
    public static final String ACTIVITY_COURSE_UPDATED = "course_updated";
    public static final String ACTIVITY_COURSE_PUBLISHED = "course_published";
    public static final String ACTIVITY_COURSE_UNPUBLISHED = "course_unpublished";
    public static final String ACTIVITY_STUDENT_ADDED = "student_added";
    public static final String ACTIVITY_STUDENT_REMOVED = "student_removed";
    public static final String ACTIVITY_CHAPTER_ADDED = "chapter_added";
    public static final String ACTIVITY_LECTURE_ADDED = "lecture_added";
    public static final String ACTIVITY_QUIZ_ADDED = "quiz_added";
    public static final String ACTIVITY_CERTIFICATE_ISSUED = "certificate_issued";

    @Transactional
    public void logActivity(String courseId, String activityType, String description) {
        try {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                return;
            }

            User user = getCurrentUser();

            CourseActivity activity = CourseActivity.builder()
                    .course(course)
                    .user(user)
                    .activityType(activityType)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .build();

            activityRepository.save(activity);
        } catch (Exception ignored) {
        }
    }

    public List<CourseActivityResponse> getActivities(String courseId, int page, int size) {
        Page<CourseActivity> activities = activityRepository.findByCourseIdOrderByTimestampDesc(
                courseId, PageRequest.of(page, size));
        
        return activities.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CourseActivityResponse> getAllActivities(String courseId) {
        List<CourseActivity> activities = activityRepository.findByCourseIdOrderByTimestampDesc(courseId);
        return activities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CourseActivityResponse toResponse(CourseActivity activity) {
        return CourseActivityResponse.builder()
                .id(activity.getId())
                .type(activity.getActivityType())
                .description(activity.getDescription())
                .userName(activity.getUser() != null ? 
                        (activity.getUser().getFullName() != null ? 
                                activity.getUser().getFullName() : 
                                activity.getUser().getEmail()) : 
                        "Hệ thống")
                .timestamp(activity.getTimestamp())
                .build();
    }

    private User getCurrentUser() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                Optional<User> user = userRepository.findById(authentication.getName());
                return user.orElse(null);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}


