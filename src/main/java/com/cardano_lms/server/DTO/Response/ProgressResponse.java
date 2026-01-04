package com.cardano_lms.server.DTO.Response;

import com.cardano_lms.server.DTO.Request.ChapterRequest;
import com.cardano_lms.server.DTO.Request.LectureRequest;
import com.cardano_lms.server.DTO.Request.PaymentOptionRequest;
import com.cardano_lms.server.DTO.Request.TestRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class ProgressResponse {
    private String id;
    private String title;
    private String imageUrl;
    private Boolean completed;
    private String instructorName;
    private String fullName;
    private Long certificateId; 

    private List<ProgressChapterResponse> chapters;
    private List<ProgressTestResponse> courseTests;
    private List<TestAndLectureCompletedResponse> testAndLectureCompleted;
}
