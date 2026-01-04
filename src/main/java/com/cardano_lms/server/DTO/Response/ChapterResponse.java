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
public class ChapterResponse {
    private Long id;
    private String title;
    private int orderIndex;

    private List<LectureResponse> lectures;
    private List<TestResponse> tests;
}
