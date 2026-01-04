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
public class QuestionRequest {
    private Long id;
    private String content;
    private Integer score;
    private String imageUrl;
    private int orderIndex;
    private String explanation;
    private List<AnswerRequest> answers;
}
