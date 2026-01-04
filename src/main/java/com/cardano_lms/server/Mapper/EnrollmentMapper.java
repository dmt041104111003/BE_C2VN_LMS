package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.EnrollCourseRequest;
import com.cardano_lms.server.DTO.Response.EnrollmentResponse;
import com.cardano_lms.server.Entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    Enrollment toEnrollment(EnrollCourseRequest enrollCourseRequest);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(target = "paymentMethod", expression = "java(enrollment.getCoursePaymentMethod()" +
            ".getPaymentMethod().getName())")
    @Mapping(source = "status", target = "status")
    EnrollmentResponse toResponse(Enrollment enrollment);
}
