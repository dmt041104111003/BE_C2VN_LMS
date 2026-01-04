package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrolledResponse {
    private String courseId;
    private String courseName;
    private String imageUrl;
    private String instructorName;
    private int numsOfStudents;
    private List<EnrolledResponse> enrolled;

}
