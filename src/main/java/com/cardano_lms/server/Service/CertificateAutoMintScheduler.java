package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Request.BatchMintRequest;
import com.cardano_lms.server.Entity.Enrollment;
import com.cardano_lms.server.Entity.Progress;
import com.cardano_lms.server.Repository.EnrollmentRepository;
import com.cardano_lms.server.Repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CertificateAutoMintScheduler {

    private static final int BATCH_SIZE = 15;

    @Value("${AUTO_MINT_ENABLED:true}")
    private boolean autoMintEnabled;

    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;
    private final CertificateService certificateService;

    @Scheduled(fixedDelayString = "${AUTO_MINT_INTERVAL_MS:60000}")
    public void processAutoMint() {
        if (!autoMintEnabled) return;

        try {
            List<Enrollment> eligible = enrollmentRepository.findEligibleForCertificate();
            if (eligible.isEmpty()) return;

            groupByCourse(eligible).forEach(this::processCourseInBatches);
        } catch (Exception ignored) {
        }
    }

    private Map<String, List<Enrollment>> groupByCourse(List<Enrollment> enrollments) {
        Map<String, List<Enrollment>> grouped = new LinkedHashMap<>();
        enrollments.forEach(e -> 
            grouped.computeIfAbsent(e.getCourse().getId(), k -> new ArrayList<>()).add(e)
        );
        return grouped;
    }

    private void processCourseInBatches(String courseId, List<Enrollment> enrollments) {
        for (int i = 0; i < enrollments.size(); i += BATCH_SIZE) {
            List<Enrollment> batch = enrollments.subList(i, Math.min(i + BATCH_SIZE, enrollments.size()));
            try {
                mintBatch(courseId, batch);
                markCertificateIssued(batch);
            } catch (Exception ignored) {
            }
        }
    }

    private void mintBatch(String courseId, List<Enrollment> enrollments) throws Exception {
        List<BatchMintRequest.BatchMintItem> items = enrollments.stream()
                .map(e -> {
                    BatchMintRequest.BatchMintItem item = new BatchMintRequest.BatchMintItem();
                    item.setEnrollmentId(e.getId());
                    item.setStudentName(e.getUser().getFullName());
                    item.setStudentWalletAddress(e.getWalletAddress());
                    return item;
                })
                .toList();

        certificateService.batchMintCertificates(courseId, items);
    }

    private void markCertificateIssued(List<Enrollment> enrollments) {
        List<Progress> progressList = enrollments.stream()
                .map(Enrollment::getProgress)
                .filter(p -> p != null)
                .peek(p -> p.setCertificateIssued(true))
                .toList();
        
        if (!progressList.isEmpty()) {
            progressRepository.saveAll(progressList);
        }
    }
}
