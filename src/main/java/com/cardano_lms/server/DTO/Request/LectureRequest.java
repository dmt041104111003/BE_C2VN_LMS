package com.cardano_lms.server.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LectureRequest {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private Integer duration;
    private Integer orderIndex;
    private String resourceUrl;
    private String resourceType;
    private Boolean previewFree;
}
