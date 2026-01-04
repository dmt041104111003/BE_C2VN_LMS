package com.cardano_lms.server.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ProgressChapterResponse {
    private Long id;
    private String title;
    private Integer orderIndex;
    private List<ProgressLectureResponse> lectures;
    private List<ProgressTestResponse> tests;
}
