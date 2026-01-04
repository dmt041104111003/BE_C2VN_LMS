package com.cardano_lms.server.Entity;

import com.cardano_lms.server.DTO.TestResult;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Progress {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JsonBackReference
    @JsonIgnore
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(columnDefinition = "TEXT")
    private String completedLectureIds = "";

    @Column(columnDefinition = "TEXT")
    private String completedTestsJson = "[]";

    private Boolean courseCompleted = false;
    private Boolean certificateIssued = false;
    private LocalDateTime lastAccessedAt;

    
    public Set<Long> getCompletedLectureIdSet() {
        if (completedLectureIds == null || completedLectureIds.isBlank()) {
            return new HashSet<>();
        }
        Set<Long> ids = new HashSet<>();
        for (String id : completedLectureIds.split(",")) {
            if (!id.isBlank()) {
                try {
                    ids.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return ids;
    }

    public void addCompletedLecture(Long lectureId) {
        Set<Long> ids = getCompletedLectureIdSet();
        ids.add(lectureId);
        completedLectureIds = String.join(",", ids.stream().sorted().map(String::valueOf).toList());
    }

    public boolean isLectureCompleted(Long lectureId) {
        return getCompletedLectureIdSet().contains(lectureId);
    }

    
    public List<TestResult> getCompletedTests() {
        if (completedTestsJson == null || completedTestsJson.isBlank() || completedTestsJson.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(completedTestsJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setCompletedTests(List<TestResult> tests) {
        try {
            completedTestsJson = objectMapper.writeValueAsString(tests);
        } catch (JsonProcessingException e) {
            completedTestsJson = "[]";
        }
    }

    public TestResult getTestResult(Long testId) {
        return getCompletedTests().stream()
                .filter(t -> t.getTestId().equals(testId))
                .findFirst()
                .orElse(null);
    }

    public boolean isTestCompleted(Long testId) {
        TestResult result = getTestResult(testId);
        return result != null && Boolean.TRUE.equals(result.getPassed());
    }

    public void addOrUpdateTestResult(Long testId, int score, boolean passed) {
        List<TestResult> tests = getCompletedTests();
        TestResult existing = tests.stream()
                .filter(t -> t.getTestId().equals(testId))
                .findFirst()
                .orElse(null);
        
        if (existing != null) {
            existing.setScore(score);
            existing.setAttempts(existing.getAttempts() + 1);
            if (passed && !Boolean.TRUE.equals(existing.getPassed())) {
                existing.setPassed(true);
                existing.setCompletedAt(LocalDate.now());
            }
        } else {
            tests.add(TestResult.builder()
                    .testId(testId)
                    .score(score)
                    .attempts(1)
                    .passed(passed)
                    .completedAt(passed ? LocalDate.now() : null)
                    .build());
        }
        setCompletedTests(tests);
    }
}
