package com.cardano_lms.server.DTO.Response;

import com.cardano_lms.server.DTO.TestResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressDashboardResponse {
    Long progressId;
    Set<Long> completedLectureIds;
    List<TestResult> testResults;
}
