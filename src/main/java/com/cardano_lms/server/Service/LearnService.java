package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Response.CourseDetailResponse;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.CourseMapper;
import com.cardano_lms.server.Repository.CourseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearnService {

    CourseRepository courseRepository;
    CourseMapper courseMapper;

    public CourseDetailResponse getCourseForLearning(String courseId) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        
        boolean enrolled = course.getEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getUser().getId().equals(userId));

        if (!enrolled) {
            throw new AppException(ErrorCode.ENROLLMENT_NOT_IN_THIS_COURSE);
        }
        
        

        
        boolean completed = course.getEnrollments().stream()
                .filter(enrollment -> enrollment.getUser().getId().equals(userId))
                .anyMatch(enrollment -> enrollment.isCompleted());

        CourseDetailResponse response = courseMapper.toDetailResponse(course);
        response.setEnrolled(enrolled);
        response.setCompleted(completed);

        return response;
    }
}
