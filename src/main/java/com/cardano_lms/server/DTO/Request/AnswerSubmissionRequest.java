package com.cardano_lms.server.DTO.Request;


import lombok.Data;

import java.util.List;

@Data
public class AnswerSubmissionRequest {
    private Long questionId;
    private List<Long> answerId;
}