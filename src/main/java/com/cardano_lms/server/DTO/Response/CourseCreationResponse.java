package com.cardano_lms.server.DTO.Response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreationResponse {
    private String id;
    private String slug;
    private String title;
    private String description;
    private String shortDescription;
    private String requirement;
    private String imageUrl;
    private String videoUrl;
    private boolean draft;
    private Integer price;
    private String currency;
    private String courseType;
    private Double discount;
    private LocalDateTime discountEndTime;
    private String policyId;

    private String instructorId;
    private String instructorName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CoursePaymentMethodResponse> coursePaymentMethods;
    private List<ChapterResponse> chapters;
    private List<TestResponse> courseTests;
    private List<CourseTagResponse> courseTags;
}
