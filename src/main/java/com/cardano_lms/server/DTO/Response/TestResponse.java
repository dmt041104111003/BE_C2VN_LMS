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
public class TestResponse {
    private Long id;
    private String title;
    private int durationMinutes;
    private String rule;
    private int passScore;
    private int orderIndex;
    private List<QuestionResponse> questions;
}
