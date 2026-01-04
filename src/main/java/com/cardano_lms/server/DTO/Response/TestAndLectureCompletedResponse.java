package com.cardano_lms.server.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class TestAndLectureCompletedResponse {
    private Long id;
    private String type;
    private Integer score;
    private Integer attempts;
    private LocalDate completedAt;
    private Boolean completed;
    private Long contentId;
}
