package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.CourseCreationRequest;
import com.cardano_lms.server.DTO.Request.CourseUpdateRequest;
import com.cardano_lms.server.DTO.Response.*;
import com.cardano_lms.server.Entity.Course;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { ChapterMapper.class, TestMapper.class, QuestionMapper.class, AnswerMapper.class, LectureMapper.class })
public interface CourseMapper {
        @Mapping(target = "chapters", ignore = true)
        @Mapping(target = "courseTests", ignore = true)
        @Mapping(target = "coursePaymentMethods", ignore = true)
        @Mapping(target = "instructor", ignore = true)
        @Mapping(target = "price", source = "price")
        @Mapping(target = "draft", source = "draft")
        @Mapping(target = "currency", source = "currency")
        @Mapping(target = "courseTags", ignore = true)
        @Mapping(target = "imageUrl", ignore = true)
        @Mapping(target = "slug", ignore = true)
        Course toCourse(CourseCreationRequest request);

        @Mapping(source = "instructor.id", target = "instructorId")
        @Mapping(source = "instructor.fullName", target = "instructorName")
        @Mapping(source = "courseTests", target = "courseTests")
        @Mapping(source = "courseTags", target = "courseTags")
        CourseCreationResponse toResponse(Course course);

        @Mapping(target = "price", source = "price")
        @Mapping(source = "instructor.fullName", target = "instructorName")
        @Mapping(source = "instructor.email", target = "instructorEmail")
        @Mapping(source = "instructor.walletAddress", target = "instructorWalletAddress")
        @Mapping(target = "numOfStudents", expression = "java(course.getEnrollments() != null ? course.getEnrollments().size() : 0)")
        @Mapping(target = "numOfLessons", expression = "java(course.getChapters() != null ? course.getChapters().stream().mapToInt(c -> c.getLectures() != null ? c.getLectures().size() : 0).sum() : 0)")
        @Mapping(target = "courseType", expression = "java(course.getCourseType().name())")
        CourseSummaryResponse toSummaryResponse(Course course);

        @Mapping(source = "instructor.id", target = "instructorId")
        @Mapping(source = "instructor.fullName", target = "instructorName")
        @Mapping(source = "instructor.bio", target = "instructorBio")
        @Mapping(source = "instructor.email", target = "instructorEmail")
        @Mapping(source = "instructor.walletAddress", target = "instructorWalletAddress")
        @Mapping(source = "courseTests", target = "courseTests")
        @Mapping(source = "courseTags", target = "courseTags")
        @Mapping(target = "courseType", expression = "java(course.getCourseType().name())")
        @Mapping(target = "instructorUserId", expression = "java(course.getInstructor().getId())")
        @Mapping(target = "numOfStudents", expression = "java(course.getEnrollments() != null ? course.getEnrollments().size() : 0)")
        CourseDetailResponse toDetailResponse(Course course);

        @Mapping(target = "coursePaymentMethods", ignore = true)
        @Mapping(target = "instructor", ignore = true)
        @Mapping(target = "courseTests", ignore = true)
        @Mapping(target = "chapters", ignore = true)
        @Mapping(target = "price", source = "price")
        @Mapping(target = "draft", source = "draft")
        @Mapping(target = "currency", source = "currency")
        @Mapping(target = "slug", ignore = true)
        void updateCourseFromRequest(CourseUpdateRequest request, @MappingTarget Course course);

        CourseUpdateResponse toCourseUpdateResponse(Course course);

        @Mapping(target = "imageUrl", source = "imageUrl")
        @Mapping(target = "draft", expression = "java(course.isDraft())")
        @Mapping(target = "courseType", source = "courseType")
        @Mapping(target = "price", source = "price")
        @Mapping(target = "discount", expression = "java(course.getDiscount() != null ? (int) Math.round(course.getDiscount()) : null)")
        @Mapping(target = "enrollmentCount", ignore = true)
        CourseShortInformationResponse toCourseShortInformationResponse(Course course);
}
