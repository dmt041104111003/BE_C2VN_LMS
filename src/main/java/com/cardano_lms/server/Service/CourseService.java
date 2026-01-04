package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Response.*;
import com.cardano_lms.server.constant.CourseType;
import com.cardano_lms.server.DTO.Request.*;
import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.*;
import com.cardano_lms.server.Repository.*;
import com.cardano_lms.server.Utils.CloudinaryUtils;
import com.cardano_lms.server.Utils.SlugUtils;
import com.cardano_lms.server.Service.payment.PaymentMethodStrategy;
import com.cardano_lms.server.constant.PredefinedPaymentMethod;
import com.cardano_lms.server.Service.helper.CourseSyncHelper;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cardano_lms.server.constant.CourseConstants.*;
import static com.cardano_lms.server.constant.CourseType.FREE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseService {

    CourseRepository courseRepository;
    PaymentMethodRepository paymentMethodRepository;
    ChapterRepository chapterRepository;
    UserRepository userRepository;
    LectureRepository lectureRepository;
    TagRepository tagRepository;
    TestRepository testRepository;
    CourseActivityRepository courseActivityRepository;
    CourseMapper courseMapper;
    TestMapper testMapper;
    ChapterMapper chapterMapper;
    LectureMapper lectureMapper;
    CloudinaryUtils cloudinaryUtils;
    List<PaymentMethodStrategy> paymentStrategies;
    CourseSyncHelper syncHelper;
    CourseQueryService queryService;
    CourseTestService testService;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public List<CourseDashboardResponse> getEducatorDashboard(String instructorId) {
        return courseRepository.findAllByInstructorId(instructorId).stream()
                .map(this::buildDashboardResponse)
                .toList();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public CourseCreationResponse createCourse(CourseCreationRequest request) throws IOException {
        User instructor = getCurrentInstructor();
        validateCourseCreation(request);

        Course course = courseMapper.toCourse(request);
        course.setSlug(SlugUtils.generateUniqueSlug(request.getTitle(), courseRepository::existsBySlug));

        uploadImage(request, course);
        setInstructorAndTimestamps(course, instructor);
        attachTags(request.getTagIds(), course);
        attachPaymentMethods(request.getPaymentMethods(), course);
        validateFreeEnrollment(request, course);
        attachCourseContent(request, course);

        Course saved = courseRepository.save(course);
        logActivity(saved, ACTIVITY_COURSE_CREATED, DESC_COURSE_CREATED + saved.getTitle());

        return courseMapper.toResponse(saved);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public CourseUpdateResponse updateCourse(String courseIdOrSlug, CourseUpdateRequest request) throws IOException {
        Course course = findOwnedCourse(courseIdOrSlug);
        validateCoursePrice(
                request.getPrice() != null ? request.getPrice() : course.getPrice(),
                request.getDiscount() != null ? request.getDiscount() : course.getDiscount()
        );
        return updateExistingCourse(course, request);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public void deleteCourse(String idOrSlug) throws IOException {
        Course course = queryService.findByIdOrSlug(idOrSlug);

        if (course.getEnrollments() != null && !course.getEnrollments().isEmpty()) {
            throw new AppException(ErrorCode.MUST_NOT_DELETE_COURSE);
        }

        if (course.getPublicIdImage() != null) {
            cloudinaryUtils.deleteImage(course.getPublicIdImage());
        }

        if (course.getCourseTags() != null) course.getCourseTags().clear();
        courseActivityRepository.deleteByCourseId(course.getId());
        courseRepository.delete(course);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public ChapterResponse addChapterToCourse(String courseId, ChapterRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Chapter newChapter = chapterMapper.toEntity(request);
        newChapter.setCourse(course);
        Chapter saved = chapterRepository.save(newChapter);

        Optional.ofNullable(request.getLectures())
                .ifPresent(list -> list.forEach(lecReq -> newChapter.addLecture(lectureMapper.toEntity(lecReq))));

        Optional.ofNullable(request.getTests())
                .ifPresent(list -> list.forEach(testReq -> syncHelper.buildTestWithQuestions(testReq, null, newChapter)));

        course.addChapter(newChapter);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        return chapterMapper.toResponse(saved);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public LectureResponse addLectureToChapter(Long chapterId, LectureRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));

        Lecture newLecture = lectureMapper.toEntity(request);
        newLecture.setChapter(chapter);
        Lecture saved = lectureRepository.save(newLecture);

        chapter.addLecture(saved);
        if (chapter.getCourse() != null) {
            chapter.getCourse().setUpdatedAt(LocalDateTime.now());
            courseRepository.save(chapter.getCourse());
        }

        return lectureMapper.toResponse(saved);
    }

    public TestResponse getTest(String courseId, Long testId) {
        if (!courseRepository.existsById(courseId)) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return testRepository.findById(testId)
                .map(testMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public TestResponse addTest(TestRequest request, Long chapterId, String courseId) {
        if (chapterId == null && courseId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        Test newTest;
        Course course = null;

        if (chapterId != null) {
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));
            newTest = syncHelper.buildTestWithQuestions(request, null, chapter);
        } else {
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
            newTest = syncHelper.buildTestWithQuestions(request, course, null);
        }

        Test saved = testRepository.save(newTest);
        if (course != null) {
            course.setUpdatedAt(LocalDateTime.now());
            courseRepository.save(course);
        }

        return testMapper.toResponse(saved);
    }

    @Transactional
    public TestResultResponse evaluateTest(TestSubmissionRequest submission, Long testId, String courseId) {
        return testService.evaluateTest(submission, testId, courseId);
    }

    public TestResultResponse getPreviousTestResult(String userId, Long testId, String courseId) {
        return testService.getPreviousTestResult(userId, testId, courseId);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public CourseSummaryResponse publishCourse(String courseId) {
        return setPublishStatus(courseId, true);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public CourseSummaryResponse unPublishCourse(String courseId) {
        return setPublishStatus(courseId, false);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public CourseDetailResponse updateDiscount(String courseId, Double discountPercent, LocalDateTime discountEndTime) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        validateOwnership(course);
        validateDiscountPercent(discountPercent);

        if (discountPercent != null) course.setDiscount(discountPercent);
        if (discountEndTime != null) course.setDiscountEndTime(discountEndTime);

        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        logActivity(course, "UPDATE_DISCOUNT",
                String.format("Cập nhật giảm giá: %.0f%% đến %s", discountPercent, discountEndTime));

        return queryService.buildDetailResponse(course, getCurrentUserId(), null);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    public void updateTestPassScore(String courseId, Long testId, int passScore) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        validateOwnership(course);

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        if (!testBelongsToCourse(test, course)) {
            throw new AppException(ErrorCode.TEST_NOT_FOUND);
        }

        if (passScore < 0 || passScore > 100) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        test.setPassScore(passScore);
        testRepository.save(test);

        logActivity(course, "UPDATE_TEST_PASS_SCORE",
                String.format("Cập nhật điểm đạt bài kiểm tra '%s': %d điểm", test.getTitle(), passScore));
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getCourses() {
        return queryService.getCourses();
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> searchCourses(String keyword, CourseType courseType,
            Integer minPrice, Integer maxPrice, String tagId, String sort, int page, int size) {
        return queryService.searchCourses(keyword, courseType, minPrice, maxPrice, tagId, sort, page, size);
    }

    @Transactional
    public CourseDetailResponse getCourseBySlug(String slug, String userId, String instructorId) {
        return queryService.getCourseBySlug(slug, userId, instructorId);
    }

    @Transactional
    public CourseDetailResponse getCourseById(String courseIdOrSlug, String userId, String instructorId) {
        return queryService.getCourseById(courseIdOrSlug, userId, instructorId);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public CourseDetailResponse getLearningContent(String slug) {
        return queryService.getLearningContent(slug);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getCoursesByProfile(String profileId, int page, int size) {
        return queryService.getCoursesByProfile(profileId, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getCoursesByProfileAll(String profileId, int page, int size) {
        return queryService.getCoursesByProfileAll(profileId, page, size);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getCoursesByProfileDrafts(String profileId, int page, int size) {
        return queryService.getCoursesByProfileDrafts(profileId, page, size);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public List<CourseShortInformationResponse> getMyShortCourses() {
        return queryService.getMyShortCourses();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public PageResponse<CourseShortInformationResponse> getMyCoursesAll(int page, int size) {
        return queryService.getMyCoursesAll(page, size);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getMyCoursesAllList() {
        return queryService.getMyCoursesAllList();
    }

    private CourseDashboardResponse buildDashboardResponse(Course course) {
        List<EnrollmentDashboardResponse> enrollmentResponses = course.getEnrollments().stream()
                .map(this::buildEnrollmentDashboard)
                .toList();

        int totalLectures = 0;
        int totalTests = course.getCourseTests() != null ? course.getCourseTests().size() : 0;
        for (Chapter chapter : course.getChapters()) {
            totalLectures += chapter.getLectures() != null ? chapter.getLectures().size() : 0;
            totalTests += chapter.getTests() != null ? chapter.getTests().size() : 0;
        }

        return new CourseDashboardResponse(course.getId(), course.getTitle(), course.getCreatedAt(),
                totalLectures, totalTests, enrollmentResponses);
    }

    private EnrollmentDashboardResponse buildEnrollmentDashboard(Enrollment enrollment) {
        Progress progress = enrollment.getProgress();
        ProgressDashboardResponse progressResponse = progress != null
                ? ProgressDashboardResponse.builder()
                        .progressId(progress.getId())
                        .completedLectureIds(progress.getCompletedLectureIdSet())
                        .testResults(progress.getCompletedTests())
                        .build()
                : null;

        User user = enrollment.getUser();
        UserDashboardResponse userResponse = new UserDashboardResponse();
        userResponse.setId(String.valueOf(user.getId()));
        userResponse.setName(user.getFullName());

        EnrollmentDashboardResponse response = new EnrollmentDashboardResponse();
        response.setId(enrollment.getId());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setCompleted(enrollment.isCompleted());
        response.setPrice(enrollment.getPrice() < 0 ? enrollment.getPrice() : 0.0);
        response.setUser(userResponse);
        response.setProgressData(progressResponse);
        return response;
    }

    private CourseUpdateResponse updateExistingCourse(Course course, CourseUpdateRequest request) throws IOException {
        updateBasicFields(course, request);
        updateImage(course, request);
        syncTags(course, request.getTagIds());
        syncPaymentMethods(course, request.getPaymentMethods());
        syncHelper.syncCourseTests(course, request.getCourseTests());
        syncHelper.syncChapters(course, request.getChapters());

        Course saved = courseRepository.save(course);
        logActivity(saved, ACTIVITY_COURSE_UPDATED, DESC_COURSE_UPDATED + saved.getTitle());

        return courseMapper.toCourseUpdateResponse(saved);
    }

    private void updateBasicFields(Course course, CourseUpdateRequest request) {
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getShortDescription() != null) course.setShortDescription(request.getShortDescription());
        if (request.getRequirement() != null) course.setRequirement(request.getRequirement());
        if (request.getVideoUrl() != null) course.setVideoUrl(request.getVideoUrl());
        if (request.getCourseType() != null) course.setCourseType(CourseType.valueOf(request.getCourseType()));
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getDiscount() != null) course.setDiscount(request.getDiscount());
        if (request.getDiscountEndTime() != null) course.setDiscountEndTime(request.getDiscountEndTime());
        course.setDraft(request.isDraft());
        course.setUpdatedAt(LocalDateTime.now());
    }

    private void updateImage(Course course, CourseUpdateRequest request) throws IOException {
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (course.getPublicIdImage() != null) {
                cloudinaryUtils.deleteImage(course.getPublicIdImage());
            }
            Map<String, Object> uploadResult = cloudinaryUtils.uploadImage(request.getImage());
            course.setImageUrl(uploadResult.get("url").toString());
            course.setPublicIdImage(uploadResult.get("public_id").toString());
        }
    }

    private void validateCourseCreation(CourseCreationRequest request) {
        if (courseRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new AppException(ErrorCode.COURSE_TITLE_EXISTED);
        }
        validateCoursePrice(request.getPrice(), request.getDiscount());
    }

    private void uploadImage(CourseCreationRequest request, Course course) throws IOException {
        if (request.getImage() == null || request.getImage().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
        try {
            Map<String, Object> uploadResult = cloudinaryUtils.uploadImage(request.getImage());
            course.setImageUrl(uploadResult.get("url").toString());
            course.setPublicIdImage(uploadResult.get("public_id").toString());
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void setInstructorAndTimestamps(Course course, User instructor) {
        course.setInstructor(instructor);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
    }

    private void attachTags(List<Long> tagIds, Course course) {
        List<Tag> tags = new ArrayList<>();
        if (tagIds != null) {
            tagIds.forEach(id -> tagRepository.findById(id).ifPresent(tags::add));
        }
        course.setCourseTags(tags);
    }

    private void attachPaymentMethods(List<PaymentOptionRequest> paymentMethods, Course course) {
        if (paymentMethods == null || paymentMethods.isEmpty()) return;
        for (PaymentOptionRequest option : paymentMethods) {
            PaymentMethodStrategy strategy = paymentStrategies.stream()
                    .filter(s -> s.supports(option.getPaymentMethodId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
            strategy.apply(course, option);
        }
    }

    private void validateFreeEnrollment(CourseCreationRequest request, Course course) {
        if (!FREE.name().equals(request.getCourseType())) return;
        boolean hasFree = course.getCoursePaymentMethods() != null && course.getCoursePaymentMethods().stream()
                .anyMatch(cpm -> {
                    String n = cpm.getPaymentMethod() != null ? cpm.getPaymentMethod().getName() : null;
                    return PredefinedPaymentMethod.FREE_ENROLL.equalsIgnoreCase(n) || "FREE".equalsIgnoreCase(n);
                });
        if (!hasFree) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
    }

    private void attachCourseContent(CourseCreationRequest request, Course course) {
        if (request.getCourseTests() != null) {
            request.getCourseTests().forEach(testReq -> syncHelper.buildTestWithQuestions(testReq, course, null));
        }
        if (request.getChapters() != null) {
            request.getChapters().forEach(chReq -> {
                Chapter chapter = chapterMapper.toEntity(chReq);
                course.addChapter(chapter);
                if (chReq.getLectures() != null) {
                    chReq.getLectures().forEach(lecReq -> chapter.addLecture(lectureMapper.toEntity(lecReq)));
                }
                if (chReq.getTests() != null) {
                    chReq.getTests().forEach(testReq -> syncHelper.buildTestWithQuestions(testReq, null, chapter));
                }
            });
        }
    }

    private void syncTags(Course course, List<Long> tagIds) {
        if (tagIds == null) return;
        List<Tag> tags = new ArrayList<>();
        tagIds.forEach(id -> tagRepository.findById(id).ifPresent(tags::add));
        course.setCourseTags(tags);
    }

    private void syncPaymentMethods(Course course, List<PaymentOptionRequest> paymentRequests) {
        if (paymentRequests == null) return;

        Set<String> requestedMethods = paymentRequests.stream()
                .map(PaymentOptionRequest::getPaymentMethodId)
                .collect(Collectors.toSet());

        course.getCoursePaymentMethods().removeIf(cpm ->
                !requestedMethods.contains(cpm.getPaymentMethod().getName()));

        Map<String, CoursePaymentMethod> existing = course.getCoursePaymentMethods().stream()
                .collect(Collectors.toMap(cpm -> cpm.getPaymentMethod().getName(), cpm -> cpm));

        for (PaymentOptionRequest option : paymentRequests) {
            String receiverAddress = resolveReceiverAddress(option);

            if (existing.containsKey(option.getPaymentMethodId())) {
                existing.get(option.getPaymentMethodId()).setReceiverAddress(receiverAddress);
            } else {
                PaymentMethod method = paymentMethodRepository.findByName(option.getPaymentMethodId())
                        .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

                course.getCoursePaymentMethods().add(CoursePaymentMethod.builder()
                        .course(course)
                        .paymentMethod(method)
                        .receiverAddress(receiverAddress)
                        .build());
            }
        }
    }

    private String resolveReceiverAddress(PaymentOptionRequest option) {
        String methodId = option.getPaymentMethodId();
        if (FREE_ENROLL_METHOD.equalsIgnoreCase(methodId) || FREE_METHOD_ALT.equalsIgnoreCase(methodId)) {
            return DEFAULT_RECEIVER_ADDRESS;
        }
        return option.getReceiverAddress() != null && !option.getReceiverAddress().isBlank()
                ? option.getReceiverAddress()
                : DEFAULT_RECEIVER_ADDRESS;
    }

    private CourseSummaryResponse setPublishStatus(String courseIdOrSlug, boolean publish) {
        Course course = findOwnedCourse(courseIdOrSlug);

        if (publish && !course.isDraft()) throw new AppException(ErrorCode.THIS_COURSE_WAS_PUBLISHED);
        if (!publish && course.isDraft()) throw new AppException(ErrorCode.THIS_COURSE_WAS_UNPUBLISHED);

        course.setDraft(!publish);
        course.setUpdatedAt(LocalDateTime.now());
        Course saved = courseRepository.save(course);

        String activityType = publish ? ACTIVITY_COURSE_PUBLISHED : ACTIVITY_COURSE_UNPUBLISHED;
        String activityDesc = publish ? DESC_COURSE_PUBLISHED : DESC_COURSE_UNPUBLISHED;
        logActivity(saved, activityType, activityDesc + saved.getTitle());

        return courseMapper.toSummaryResponse(saved);
    }

    private Course findOwnedCourse(String courseIdOrSlug) {
        Course course = queryService.findByIdOrSlug(courseIdOrSlug);
        validateOwnership(course);
        return course;
    }

    private void validateOwnership(Course course) {
        String currentInstructorId = getCurrentUserId();
        if (currentInstructorId == null || course.getInstructor() == null
                || !currentInstructorId.equals(course.getInstructor().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private boolean testBelongsToCourse(Test test, Course course) {
        if (test.getCourse() != null && test.getCourse().getId().equals(course.getId())) return true;
        if (test.getChapter() != null) {
            for (Chapter chapter : course.getChapters()) {
                if (chapter.getId().equals(test.getChapter().getId())) return true;
            }
        }
        return false;
    }

    private void validateCoursePrice(Integer price, Double discount) {
        if (price == null || price < 0) return;
        if (price > 0 && price < MIN_PAID_PRICE_ADA) {
            throw new AppException(ErrorCode.INVALID_COURSE_PRICE);
        }
        if (price > 0 && discount != null && discount > 0) {
            double discountedPrice = price * (1 - discount / 100);
            if (discountedPrice > 0 && discountedPrice < MIN_PAID_PRICE_ADA) {
                throw new AppException(ErrorCode.INVALID_COURSE_PRICE);
            }
        }
    }

    private void validateDiscountPercent(Double discountPercent) {
        if (discountPercent != null && (discountPercent < 0 || discountPercent > 100)) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
    }

    private void logActivity(Course course, String activityType, String description) {
        try {
            User user = getCurrentUser();
            courseActivityRepository.save(CourseActivity.builder()
                    .course(course)
                    .user(user)
                    .activityType(activityType)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception ignored) {}
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() ? auth.getName() : null;
    }

    private User getCurrentInstructor() {
        String userId = getCurrentUserId();
        if (userId == null) throw new AppException(ErrorCode.YOU_ARE_NOT_INSTRUCTOR);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.YOU_ARE_NOT_INSTRUCTOR));
    }

    private User getCurrentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                return userRepository.findById(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
