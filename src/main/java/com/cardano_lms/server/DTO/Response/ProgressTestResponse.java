package com.cardano_lms.server.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProgressTestResponse {
    private Long id;
    private String title;
    private int orderIndex;
}
