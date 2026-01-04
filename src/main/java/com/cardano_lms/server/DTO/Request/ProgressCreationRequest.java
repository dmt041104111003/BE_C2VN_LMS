package com.cardano_lms.server.DTO.Request;

import com.cardano_lms.server.constant.CourseContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressCreationRequest {
    private CourseContentType type;
    private Integer score;
    private Long lectureId;
    private Long testId;
}
