package com.cardano_lms.server.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TestResultResponse {
    private Long testId;
    private String courseId;
    private String userId;
    private int totalQuestions;
    private int correctAnswers;
    private int passScore;
    private int maxScore;
    private double score;
    private boolean passed;
    private List<QuestionResultResponse> questionResults;
}