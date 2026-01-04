package com.cardano_lms.server.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    private Long testId;
    private Integer score;
    private Integer attempts;
    private LocalDate completedAt;
    private Boolean passed;
}







