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
public class CourseDetailResponse {
    private String id;
    private String slug;
    private String title;
    private String description;
    private String shortDescription;
    private String requirement;
    private String imageUrl;
    private String videoUrl;
    private Double discount;
    private boolean draft;
    private Integer price;
    private String currency;
    private LocalDateTime discountEndTime;
    private String policyId;
    private String instructorId;
    private String instructorUserId;
    private String courseType;
    private String instructorName;
    private String instructorBio;
    private String instructorEmail;
    private String instructorWalletAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean enrolled;
    private boolean completed;
    private Double rating;
    private int numOfStudents;
    List<TestResponse> courseTests;
    List<ChapterSummaryResponse> chapters;
    List<CourseTagResponse> courseTags;
    List<CoursePaymentMethodResponse> coursePaymentMethods;

}
