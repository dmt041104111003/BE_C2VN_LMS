package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSummaryResponse {
    private String id;
    private String slug;
    private String title;
    private String description;
    private String shortDescription;
    private String requirement;
    private String imageUrl;
    private String videoUrl;
    private int numOfStudents;
    private int numOfLessons;
    private Double discount;
    private boolean draft;
    private Integer price;
    private String currency;
    private String totalTime;
    private LocalDateTime discountEndTime;
    private String policyId;
    private String instructorName;
    private String instructorEmail;
    private String instructorWalletAddress;
    private String courseType;
    private List<CourseTagResponse> courseTags;
    private Double rating;
}
