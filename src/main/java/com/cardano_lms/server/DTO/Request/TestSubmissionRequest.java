package com.cardano_lms.server.DTO.Request;

import lombok.Data;

import java.util.List;

@Data
public class TestSubmissionRequest {
    private String userId;
    private List<AnswerSubmissionRequest> answers;
}