package com.cardano_lms.server.DTO.Request;

import com.cardano_lms.server.Entity.Chapter;
import com.cardano_lms.server.Entity.Course;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class TestRequest {
    private Long id;
    private String title;
    private Integer durationMinutes;
    private String rule;
    private int passScore;
    private int orderIndex;
    private List<QuestionRequest> questions;
}

