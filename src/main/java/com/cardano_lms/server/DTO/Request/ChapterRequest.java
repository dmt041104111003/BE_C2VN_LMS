package com.cardano_lms.server.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChapterRequest {
    private Long id;
    private String title;
    private Integer orderIndex;
    private List<LectureRequest> lectures;
    private List<TestRequest> tests;
}
