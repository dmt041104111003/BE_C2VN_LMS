package com.cardano_lms.server.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionResultResponse {
    private Long questionId;
    private List<Long> correctAnswerIds;
    private List<Long> selectedAnswerIds;
    private boolean isCorrect;
    private String explanation;
}







