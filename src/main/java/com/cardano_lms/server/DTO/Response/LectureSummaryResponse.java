package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureSummaryResponse {
    private Long id;
    private String title;
    private String videoUrl;
    private int time;
    private String description;
    private int orderIndex;
    private String resourceUrl;
    private String resourceType;
    private Boolean previewFree;
}
