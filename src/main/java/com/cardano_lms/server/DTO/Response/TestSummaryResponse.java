package com.cardano_lms.server.DTO.Response;

import lombok.*;
import org.springframework.boot.convert.DataSizeUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestSummaryResponse{
    private Long id;
    private String title;
    private int durationMinutes;
    private int orderIndex;

}