package com.cardano_lms.server.Service;

import com.cardano_lms.server.constant.*;
import com.cardano_lms.server.DTO.Request.EnrollCourseRequest;
import com.cardano_lms.server.DTO.Request.PayAndEnrollRequest;
import com.cardano_lms.server.DTO.Response.*;
import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.EnrollmentMapper;
import com.cardano_lms.server.Mapper.ProgressViewMapper;
import com.cardano_lms.server.Repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CoursePaymentMethodRepository coursePaymentMethodRepository;
    private final ProgressRepository progressRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final ProgressViewMapper progressViewMapper;
    private final CertificateRepository certificateRepository;
    private final SnapshotService snapshotService;
    private final EnrolledSnapshotRepository snapshotRepository;
    private final UserAnswerRepository userAnswerRepository;

    @Value("${BLOCKFROST_PROJECT_ID}")
    private String blockfrostProjectId;

    @Value("${BLOCKFROST_API}")
    private String blockfrostApi;

    public enum VerifyResult {
        SUCCESS,
        TIMEOUT,
        INVALID_SENDER,
        INVALID_RECEIVER,
        INVALID_AMOUNT,
        ERROR
    }

    public List<MyEnrollmentResponse> getMyEnrollments() {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        List<Enrollment> enrollments = enrollmentRepository.findAllByUser_Id(userId);
        
        return enrollments.stream().map(e -> {
            Course course = e.getCourse();
            EnrolledSnapshot snapshot = snapshotRepository.findByEnrollmentId(e.getId()).orElse(null);
            
            int totalItems = snapshot != null 
                    ? calculateTotalItemsFromSnapshot(snapshot)
                    : calculateTotalLecturesAndTests(course);
            long completedItems = countCompletedItems(e);
            int progressPercent = totalItems > 0 ? Math.min((int) ((completedItems * 100) / totalItems), 100) : 0;
            int completedLectures = countCompletedLectures(e);
            
            return MyEnrollmentResponse.builder()
                    .enrollmentId(e.getId())
                    .courseId(course.getId())
                    .courseSlug(course.getSlug())
                    .courseTitle(course.getTitle())
                    .courseImage(course.getImageUrl())
                    .instructorName(course.getInstructor() != null ? course.getInstructor().getFullName() : null)
                    .enrolledAt(e.getEnrolledAt())
                    .progressPercent(progressPercent)
                    .completed(e.isCompleted())
                    .completedLectures(completedLectures)
                    .walletAddress(e.getWalletAddress())
                    .build();
        }).toList();
    }
    
    @SuppressWarnings("unchecked")
    private int calculateTotalItemsFromSnapshot(EnrolledSnapshot snapshot) {
        try {
            var structure = snapshotService.parseStructure(snapshot.getStructureJson());
            List<java.util.Map<String, Object>> chapters = (List<java.util.Map<String, Object>>) 
                    structure.getOrDefault("chapters", java.util.Collections.emptyList());
            List<java.util.Map<String, Object>> courseTests = (List<java.util.Map<String, Object>>) 
                    structure.getOrDefault("courseTests", java.util.Collections.emptyList());
            
            int totalLectures = 0;
            int totalTests = courseTests.size();
            
            for (var chapter : chapters) {
                List<?> lectures = (List<?>) chapter.getOrDefault("lectures", java.util.Collections.emptyList());
                List<?> tests = (List<?>) chapter.getOrDefault("tests", java.util.Collections.emptyList());
                totalLectures += lectures.size();
                totalTests += tests.size();
            }
            
            return totalLectures + totalTests;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long countCompletedItems(Enrollment enrollment) {
        Progress progress = progressRepository.findByEnrollment_Id(enrollment.getId()).orElse(null);
        if (progress == null) return 0;
        
        int lecturesCompleted = progress.getCompletedLectureIdSet().size();
        int testsCompleted = (int) progress.getCompletedTests().stream()
                .filter(t -> Boolean.TRUE.equals(t.getPassed()))
                .count();
        
        return lecturesCompleted + testsCompleted;
    }
    
    private int countCompletedLectures(Enrollment enrollment) {
        Progress progress = progressRepository.findByEnrollment_Id(enrollment.getId()).orElse(null);
        if (progress == null) return 0;
        return progress.getCompletedLectureIdSet().size();
    }

    private String getCurrentInstructorId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getName();
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public ProgressResponse getEnrollmentProgressByInstructor(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(
                () -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        if (!getCurrentInstructorId().equals(enrollment.getCourse().getInstructor().getId()))
            throw new AppException(ErrorCode.YOU_ARE_NOT_INSTRUCTOR_OF_THIS_COURSE);
        return progressViewMapper.toProgressResponse(enrollment);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public CourseEnrolledResponse getAllEnrolledByCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        String instructorId = getCurrentInstructorId();
        if (!instructorId.equals(course.getInstructor().getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        List<EnrolledResponse> enrolledResponses = course.getEnrollments().stream()
                .map(enrollment -> buildEnrolledResponse(enrollment, course))
                .toList();

        return CourseEnrolledResponse.builder()
                .courseId(course.getId())
                .courseName(course.getTitle())
                .imageUrl(course.getImageUrl())
                .instructorName(course.getInstructor().getFullName())
                .numsOfStudents(enrolledResponses.size())
                .enrolled(enrolledResponses)
                .build();
    }

    public boolean verifyPayment(String expectedSender, String expectedReceiver,
            double expectedAmountAda, String txHash) {
        try {
            String base = blockfrostApi;
            String txUrl = base + "/txs/" + txHash;
            String utxoUrl = txUrl + "/utxos";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("project_id", blockfrostProjectId);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            int attempts = 0;
            int maxAttempts = 18;
            while (attempts < maxAttempts) {
                try {
                    ResponseEntity<String> txResp = restTemplate.exchange(txUrl, HttpMethod.GET, entity, String.class);
                    if (txResp.getStatusCode().is2xxSuccessful()) {
                        break;
                    }
                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                        throw e;
                    }
                }
                attempts++;
                if (attempts < maxAttempts) {
                    Thread.sleep(10_000);
                }
            }
            if (attempts >= maxAttempts) {
                return false;
            }

            Thread.sleep(2000);

            ResponseEntity<String> response = restTemplate.exchange(utxoUrl, HttpMethod.GET, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());

            JsonNode inputs = json.get("inputs");
            JsonNode outputs = json.get("outputs");
            if (inputs == null || !inputs.elements().hasNext() || outputs == null || !outputs.elements().hasNext()) {
                return false;
            }

            long expectedLovelaceLong = Math.round(expectedAmountAda * 1_000_000L);
            BigDecimal expectedLovelace = BigDecimal.valueOf(expectedLovelaceLong);

            String normalizedExpectedSender = expectedSender.trim().toLowerCase();
            String normalizedExpectedReceiver = expectedReceiver.trim().toLowerCase();

            boolean senderMatched = false;
            for (JsonNode input : inputs) {
                String senderAddress = input.path("address").asText("").trim().toLowerCase();
                if (!senderAddress.isBlank() && senderAddress.equals(normalizedExpectedSender)) {
                    senderMatched = true;
                    break;
                }
            }

            BigDecimal receiverAmount = BigDecimal.ZERO;
            boolean receiverMatched = false;
            for (JsonNode output : outputs) {
                String outputAddress = output.path("address").asText("").trim().toLowerCase();
                if (outputAddress.equals(normalizedExpectedReceiver)) {
                    receiverMatched = true;
                    for (JsonNode amount : output.path("amount")) {
                        if ("lovelace".equals(amount.path("unit").asText(""))) {
                            receiverAmount = receiverAmount.add(new BigDecimal(amount.path("quantity").asText("0")));
                        }
                    }
                }
            }

            if (!senderMatched) {
                return false;
            }
            if (!receiverMatched) {
                return false;
            }
            BigDecimal tolerance = expectedLovelace.multiply(BigDecimal.valueOf(0.01));
            BigDecimal minAcceptable = expectedLovelace.subtract(tolerance);
            
            if (receiverAmount.compareTo(minAcceptable) < 0) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public Enrollment createEnrollment(String userId, String courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return createEnrollmentForUser(user, courseId);
    }
    
    @Transactional
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public Enrollment addStudentByContact(String courseId, String contactType, String contactValue) {
        User user;
        if ("email".equalsIgnoreCase(contactType)) {
            user = userRepository.findByEmailIgnoreCase(contactValue)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        } else if ("wallet".equalsIgnoreCase(contactType)) {
            user = userRepository.findByWalletAddress(contactValue)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        } else {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        return createEnrollmentForUser(user, courseId);
    }
    
    private Enrollment createEnrollmentForUser(User user, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        String instructorId = getCurrentInstructorId();
        if (!instructorId.equals(course.getInstructor().getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        String currentUserId = getCurrentUserId();
        if (currentUserId.equals(user.getId())) {
            throw new AppException(ErrorCode.YOU_ARE_INSTRUCTOR);
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new AppException(ErrorCode.ALREADY_JOIN_THIS_COURSE);
        }

        CoursePaymentMethod defaultMethod = course.getCoursePaymentMethods().isEmpty()
                ? null
                : course.getCoursePaymentMethods().get(0);
        
        Enrollment enrollment = Enrollment.builder()
                .enrolledAt(LocalDateTime.now())
                .completed(false)
                .price(0)
                .coursePaymentMethod(defaultMethod)
                .orderId(UUID.randomUUID().toString())
                .status(OrderStatus.SUCCESS)
                .course(course)
                .user(user)
                .build();

        course.getEnrollments().add(enrollment);
        Enrollment saved = enrollmentRepository.save(enrollment);
        
        
        snapshotService.createSnapshot(saved, course);
        
        return saved;
    }

    @Transactional
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void deleteEnrollment(Long enrollmentId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        String instructorId = getCurrentInstructorId();
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.getCourse().getId().equals(courseId)) {
            throw new AppException(ErrorCode.ENROLLMENT_NOT_IN_THIS_COURSE);
        }
        enrollmentRepository.delete(enrollment);
    }

    @Transactional
    public void deleteMyEnrollment(Long enrollmentId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        String courseId = enrollment.getCourse().getId();
        
        snapshotRepository.deleteByEnrollmentId(enrollmentId);
        progressRepository.deleteByEnrollment_Id(enrollmentId);
        userAnswerRepository.deleteByUserIdAndCourseId(userId, courseId);
        enrollmentRepository.delete(enrollment);
    }

    @Transactional
    public EnrollmentResponse createEnrollmentAfterPayment(EnrollCourseRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (enrollmentRepository.existsByUserIdAndCourseId(request.getUserId(), request.getCourseId())) {
            throw new AppException(ErrorCode.ALREADY_JOIN_THIS_COURSE);
        }

        if (course.getInstructor() != null 
                && course.getInstructor().getId().equals(request.getUserId())) {
            throw new AppException(ErrorCode.YOU_ARE_INSTRUCTOR);
        }

        CoursePaymentMethod method;
        double expectedPrice;

        if (course.getCourseType().equals(CourseType.FREE)) {
            
            method = course.getCoursePaymentMethods().stream()
                    .filter(m -> PredefinedPaymentMethod.FREE_ENROLL
                            .equals(m.getPaymentMethod().getName()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_HAVE_METHOD));

            expectedPrice = 0.0;
        } else {
            
            Long paymentMethodId = Long.parseLong(request.getCoursePaymentMethodId());
            method = coursePaymentMethodRepository.findById(paymentMethodId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_HAVE_METHOD));

            expectedPrice = course.getPrice();

            if (course.getDiscount() != null
                    && course.getDiscountEndTime() != null
                    && LocalDateTime.now().isBefore(course.getDiscountEndTime())) {
                expectedPrice = expectedPrice - (course.getDiscount() / 100 * expectedPrice);
            }

            if (request.getSenderAddress() == null || request.getSenderAddress().isBlank()) {
                throw new AppException(ErrorCode.MISSING_ARGUMENT);
            }
            if (request.getTxHash() == null || request.getTxHash().isBlank()) {
                throw new AppException(ErrorCode.MISSING_ARGUMENT);
            }

            double tolerance = expectedPrice * 0.01;
            if (Math.abs(request.getPriceAda() - expectedPrice) > tolerance) {
                throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
            }

        }
        
        Enrollment enrollment = enrollmentMapper.toEnrollment(request);
        
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setCoursePaymentMethod(method);
        enrollment.setPrice(expectedPrice);
        enrollment.setOrderId(UUID.randomUUID().toString());
        enrollment.setStatus(OrderStatus.SUCCESS);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setTxHash(request.getTxHash());
        enrollment.setSenderAddress(request.getSenderAddress());
        enrollment.setWalletAddress(request.getSenderAddress());
        

        Enrollment saved = enrollmentRepository.save(enrollment);
        
        
        snapshotService.createSnapshot(saved, course);
        
        return enrollmentMapper.toResponse(saved);
    }

    public List<PaymentHistoryResponse> getPaymentHistoryByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        List<Enrollment> enrollmentList = enrollmentRepository.findAllByUser_Id(userId);

        return enrollmentList.stream().map(enrollment -> PaymentHistoryResponse.builder()
                .enrolledAt(enrollment.getEnrolledAt())
                .completed(enrollment.isCompleted())
                .coursePaymentMethodName(enrollment.getCoursePaymentMethod() != null
                        ? enrollment.getCoursePaymentMethod().getPaymentMethod().getName()
                        : null)
                .status(enrollment.getStatus())
                .orderId(enrollment.getOrderId())
                .price(enrollment.getPrice())
                .courseTitle(enrollment.getCourse() != null ? enrollment.getCourse().getTitle() : null)
                .imageUrl(enrollment.getCourse() != null ? enrollment.getCourse().getImageUrl() : null)
                .build()).toList();
    }

    @Transactional
    public void updateCourseCompletionStatus(String userId, String courseId) {
        Enrollment enrollment = enrollmentRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (enrollment.isCompleted())
            throw new AppException(ErrorCode.ALREADY_COMPLETED_COURSERROR_CODE);
        Course course = enrollment.getCourse();
        int totalItems = calculateTotalLecturesAndTests(course);
        long completedItems = countCompletedItems(enrollment);

        if (totalItems > 0 && completedItems == totalItems) {
            enrollment.setCompleted(true);
            Progress progress = enrollment.getProgress();
            if (progress != null) {
                progress.setCourseCompleted(true);
            }
            enrollmentRepository.save(enrollment);
        } else {
            throw new AppException(ErrorCode.COURSE_NOT_COMPLETED);
        }
    }

    private int calculateTotalLecturesAndTests(Course course) {
        return calculateTotalLectures(course) + calculateTotalTests(course);
    }

    private int calculateTotalLectures(Course course) {
        return course.getChapters() != null
                ? course.getChapters().stream().mapToInt(ch -> ch.getLectures() != null ? ch.getLectures().size() : 0).sum()
                : 0;
    }

    private int calculateTotalTests(Course course) {
        int totalTestsInChapters = course.getChapters() != null
                ? course.getChapters().stream().mapToInt(ch -> ch.getTests() != null ? ch.getTests().size() : 0).sum()
                : 0;
        int totalFinalTests = course.getCourseTests() != null ? course.getCourseTests().size() : 0;
        return totalTestsInChapters + totalFinalTests;
    }

    private EnrolledResponse buildEnrolledResponse(Enrollment enrollment, Course course) {
        int totalLectures = calculateTotalLectures(course);
        int totalTests = calculateTotalTests(course);
        
        Progress progress = enrollment.getProgress();
        
        int lecturesCompleted = progress != null 
                ? progress.getCompletedLectureIdSet().size() 
                : 0;
        int testsCompleted = progress != null 
                ? (int) progress.getCompletedTests().stream().filter(t -> Boolean.TRUE.equals(t.getPassed())).count()
                : 0;
        
        int lectureProgressPercent = totalLectures > 0 
                ? (lecturesCompleted * 100) / totalLectures 
                : 0;
        
        boolean allLecturesCompleted = totalLectures > 0 && lecturesCompleted >= totalLectures;
        boolean allTestsCompleted = totalTests == 0 || testsCompleted >= totalTests;
        boolean courseCompleted = allLecturesCompleted && allTestsCompleted;
        boolean hasCertificate = enrollment.getCertificate() != null;
        
        return EnrolledResponse.builder()
                .enrolledId(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .userName(enrollment.getUser().getFullName())
                .email(enrollment.getUser().getEmail())
                .walletAddress(enrollment.getWalletAddress())
                .enrollAt(enrollment.getEnrolledAt())
                .totalLectures(totalLectures)
                .totalTests(totalTests)
                .lecturesCompleted(lecturesCompleted)
                .testsCompleted(testsCompleted)
                .lectureProgressPercent(lectureProgressPercent)
                .allLecturesCompleted(allLecturesCompleted)
                .allTestsCompleted(allTestsCompleted)
                .courseCompleted(courseCompleted)
                .hasCertificate(hasCertificate)
                .build();
    }

    
    @PreAuthorize("isAuthenticated()")
    public CourseUpgradeInfoResponse checkCourseUpgrade(String courseId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Enrollment enrollment = enrollmentRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.HAVE_NOT_JOIN_THIS_COURSE));
        
        EnrolledSnapshot snapshot = snapshotRepository.findByEnrollmentId(enrollment.getId())
                .orElse(null);
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        boolean hasNewVersion = snapshotService.hasNewVersion(snapshot, course);
        
        return CourseUpgradeInfoResponse.builder()
                .courseId(courseId)
                .hasNewVersion(hasNewVersion)
                .currentSnapshotVersion(snapshot != null ? snapshot.getVersion() : 0)
                .snapshotCreatedAt(snapshot != null ? snapshot.getCreatedAt() : null)
                .courseUpdatedAt(course.getUpdatedAt())
                .build();
    }

    
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public CourseUpgradeInfoResponse upgradeCourseSnapshot(String courseId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Enrollment enrollment = enrollmentRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.HAVE_NOT_JOIN_THIS_COURSE));
        
        EnrolledSnapshot snapshot = snapshotRepository.findByEnrollmentId(enrollment.getId())
                .orElse(null);
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        
        initializeCourseCollections(course);

        if (snapshot == null) {
            
            snapshot = snapshotService.createSnapshot(enrollment, course);
        } else {
            
            snapshot = snapshotService.upgradeSnapshot(snapshot, course);
        }

        
        enrollment.setProgress(null);
        enrollment.setCompleted(false);
        enrollmentRepository.save(enrollment);
        progressRepository.deleteByEnrollment_Id(enrollment.getId());
        
        
        userAnswerRepository.deleteByUserIdAndCourseId(userId, courseId);

        return CourseUpgradeInfoResponse.builder()
                .courseId(courseId)
                .hasNewVersion(false)
                .currentSnapshotVersion(snapshot.getVersion())
                .snapshotCreatedAt(snapshot.getCreatedAt())
                .courseUpdatedAt(course.getUpdatedAt())
                .message("Đã nâng cấp khóa học lên phiên bản mới. Tiến độ đã được đặt lại.")
                .build();
    }

    private void initializeCourseCollections(Course course) {
        if (course.getChapters() != null) {
            org.hibernate.Hibernate.initialize(course.getChapters());
            for (var chapter : course.getChapters()) {
                if (chapter.getLectures() != null) {
                    org.hibernate.Hibernate.initialize(chapter.getLectures());
                }
                if (chapter.getTests() != null) {
                    org.hibernate.Hibernate.initialize(chapter.getTests());
                    for (var test : chapter.getTests()) {
                        if (test.getQuestions() != null) {
                            org.hibernate.Hibernate.initialize(test.getQuestions());
                            for (var q : test.getQuestions()) {
                                if (q.getAnswers() != null) {
                                    org.hibernate.Hibernate.initialize(q.getAnswers());
                                }
                            }
                        }
                    }
                }
            }
        }
        if (course.getCourseTests() != null) {
            org.hibernate.Hibernate.initialize(course.getCourseTests());
            for (var test : course.getCourseTests()) {
                if (test.getQuestions() != null) {
                    org.hibernate.Hibernate.initialize(test.getQuestions());
                    for (var q : test.getQuestions()) {
                        if (q.getAnswers() != null) {
                            org.hibernate.Hibernate.initialize(q.getAnswers());
                        }
                    }
                }
            }
        }
    }
}
