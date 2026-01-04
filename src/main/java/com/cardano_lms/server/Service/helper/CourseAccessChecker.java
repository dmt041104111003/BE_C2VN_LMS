package com.cardano_lms.server.Service.helper;

import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.Enrollment;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CourseAccessChecker {

    private CourseAccessChecker() {}

    public static boolean isCurrentUserInstructor(Course course) {
        String courseInstructorUserId = extractInstructorUserId(course);
        String currentUserId = getCurrentUserId();
        
        if (currentUserId == null || courseInstructorUserId == null) {
            return false;
        }
        
        return currentUserId.equals(courseInstructorUserId);
    }

    public static boolean isUserEnrolled(Course course, String userId) {
        if (userId == null || course.getEnrollments() == null) {
            return false;
        }
        
        return course.getEnrollments().stream()
                .anyMatch(e -> userId.equals(e.getUser().getId()));
    }

    public static boolean hasUserCompleted(Course course, String userId) {
        if (userId == null || course.getEnrollments() == null) {
            return false;
        }
        
        return course.getEnrollments().stream()
                .filter(e -> userId.equals(e.getUser().getId()))
                .anyMatch(Enrollment::isCompleted);
    }

    public static boolean canUserSeeCorrectAnswers(Course course, String userId) {
        return isCurrentUserInstructor(course) || hasUserCompleted(course, userId);
    }

    private static String extractInstructorUserId(Course course) {
        if (course.getInstructor() == null) return null;
        return course.getInstructor().getId();
    }

    private static String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }
}
