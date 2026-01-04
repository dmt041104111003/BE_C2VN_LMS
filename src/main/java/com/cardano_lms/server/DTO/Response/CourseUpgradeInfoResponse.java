package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpgradeInfoResponse {
    private String courseId;
    private boolean hasNewVersion;
    private Integer currentSnapshotVersion;
    private LocalDateTime snapshotCreatedAt;
    private LocalDateTime courseUpdatedAt;
    private String message;
}



