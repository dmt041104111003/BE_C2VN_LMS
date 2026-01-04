package com.cardano_lms.server.DTO.Response;

import com.cardano_lms.server.DTO.Request.ChapterRequest;
import com.cardano_lms.server.DTO.Request.PaymentOptionRequest;
import com.cardano_lms.server.DTO.Request.TestRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseUpdateResponse {
    private String id;
    private String slug;
    private String title;
    private String description;
    private String shortDescription;
    private String requirement;
    private MultipartFile image;
    private String videoUrl;
    private boolean draft;
    private Double discount;
    private Integer price;
    private String currency;
    private String courseType;
    private LocalDateTime discountEndTime;
    private String policyId;
    private String instructorId;

    private List<ChapterRequest> chapters;
    private List<PaymentOptionRequest> paymentMethods;
    private List<TestRequest> courseTests;
    private List<Long> tagIds;

}
