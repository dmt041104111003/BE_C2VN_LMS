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
public class QuestionResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private int orderIndex;
    private int score;
    private String explanation;

    private List<AnswerResponse> answers;
}
