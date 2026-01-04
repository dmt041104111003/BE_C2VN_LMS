package com.cardano_lms.server.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProgressLectureResponse {
    private Long id;
    private String title;
    private int orderIndex;
    private String resourceUrl;
    private Integer duration;
}
