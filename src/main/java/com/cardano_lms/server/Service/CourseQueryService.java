package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Response.*;
import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.CourseMapper;
import com.cardano_lms.server.Repository.*;
import com.cardano_lms.server.Service.helper.AnswerVisibilityHandler;
import com.cardano_lms.server.Service.helper.CourseAccessChecker;
import com.cardano_lms.server.Service.helper.CourseDurationCalculator;
import com.cardano_lms.server.constant.CourseType;
import com.cardano_lms.server.constant.FeedbackStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.cardano_lms.server.constant.CourseConstants.SORT_POPULAR;
import static com.cardano_lms.server.Specification.CourseSpecification.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseQueryService {

    CourseRepository courseRepository;
    EnrollmentRepository enrollmentRepository;
    EnrolledSnapshotRepository snapshotRepository;
    UserAnswerRepository userAnswerRepository;
    FeedbackRepository feedbackRepository;
    CourseMapper courseMapper;
    SnapshotService snapshotService;

    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getCourses() {
        List<Course> courses = courseRepository.findAllByDraftFalse();
        return buildSummariesWithRatings(courses);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> searchCourses(
            String keyword, CourseType courseType, Integer minPrice, Integer maxPrice,
            String tagId, String sort, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Course> spec = buildSearchSpec(keyword, courseType, minPrice, maxPrice, tagId);

        Page<Course> result = courseRepository.findAll(spec, pageable);
        List<Course> content = sortByPopularityIfNeeded(result.getContent(), sort);
        List<CourseSummaryResponse> summaries = buildSummariesWithRatings(content);

        return buildPageResponse(summaries, result);
    }

    @Transactional
    public CourseDetailResponse getCourseBySlug(String slug, String userId, String instructorId) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return buildDetailResponse(course, userId, instructorId);
    }

    @Transactional
    public CourseDetailResponse getCourseById(String courseIdOrSlug, String userId, String instructorId) {
        Course course = findByIdOrSlug(courseIdOrSlug);
        return buildDetailResponse(course, userId, instructorId);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public CourseDetailResponse getLearningContent(String slug) {
        String currentUserId = getCurrentUserId();
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        boolean isInstructor = CourseAccessChecker.isCurrentUserInstructor(course);
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUserId, course.getId());

        if (!isInstructor && !isEnrolled) {
            throw new AppException(ErrorCode.NOT_ENROLLED);
        }

        if (isInstructor) {
            return buildDetailResponse(course, currentUserId, null);
        }

        return buildLearnerContent(course, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getCoursesByProfile(String profileId, int page, int size) {
        Page<Course> coursePage = courseRepository.findAllByInstructorIdAndDraftFalse(profileId, PageRequest.of(page, size));
        return buildSummaryPageResponse(coursePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getCoursesByProfileAll(String profileId, int page, int size) {
        Page<Course> coursePage = courseRepository.findAllByInstructorId(profileId, PageRequest.of(page, size));
        return buildSummaryPageResponse(coursePage);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getCoursesByProfileDrafts(String profileId, int page, int size) {
        validateInstructorOwnership(profileId);
        Page<Course> coursePage = courseRepository.findAllByInstructorIdAndDraftTrue(profileId, PageRequest.of(page, size));
        return buildSummaryPageResponse(coursePage);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public List<CourseShortInformationResponse> getMyShortCourses() {
        String educatorId = getCurrentUserId();
        return courseRepository.findAllByInstructorIdAndDraftFalse(educatorId).stream()
                .map(courseMapper::toCourseShortInformationResponse)
                .toList();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public PageResponse<CourseShortInformationResponse> getMyCoursesAll(int page, int size) {
        String instructorId = getCurrentUserId();
        Page<Course> coursePage = courseRepository.findAllByInstructorId(instructorId, PageRequest.of(page, size));

        List<CourseShortInformationResponse> content = coursePage.getContent().stream()
                .map(course -> {
                    CourseShortInformationResponse response = courseMapper.toCourseShortInformationResponse(course);
                    response.setEnrollmentCount(course.getEnrollments() != null ? course.getEnrollments().size() : 0);
                    return response;
                })
                .toList();

        return PageResponse.<CourseShortInformationResponse>builder()
                .content(content)
                .page(coursePage.getNumber())
                .size(coursePage.getSize())
                .totalElements(coursePage.getTotalElements())
                .totalPages(coursePage.getTotalPages())
                .build();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getMyCoursesAllList() {
        String educatorId = getCurrentUserId();
        return courseRepository.findAllByInstructorId(educatorId).stream()
                .map(courseMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public Course findByIdOrSlug(String idOrSlug) {
        return courseRepository.findById(idOrSlug)
                .or(() -> courseRepository.findBySlug(idOrSlug))
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    }

    public CourseDetailResponse buildDetailResponse(Course course, String userId, String instructorId) {
        boolean isInstructor = CourseAccessChecker.isCurrentUserInstructor(course);
        boolean enrolled = CourseAccessChecker.isUserEnrolled(course, userId);

        if (course.isDraft() && !isInstructor && !enrolled) {
            throw new AppException(ErrorCode.THIS_COURSE_IS_PRIVATE);
        }

        boolean completed = CourseAccessChecker.hasUserCompleted(course, userId);
        initializeLazyCollections(course);

        CourseDetailResponse response = courseMapper.toDetailResponse(course);
        response.setEnrolled(enrolled);
        response.setCompleted(completed);

        if (course.getInstructor() != null) {
            response.setInstructorBio(course.getInstructor().getBio());
        }

        setAverageRating(course, response);

        if (!enrolled && !isInstructor) {
            hideNonPreviewContent(response);
        }

        setPaymentMethods(course, response);

        boolean canSeeAnswers = isInstructor || completed;
        List<UserAnswer> userAnswers = userId != null ? userAnswerRepository.findByUserId(userId) : List.of();
        AnswerVisibilityHandler.applyVisibility(course, response, canSeeAnswers, userAnswers);

        return response;
    }

    private CourseDetailResponse buildLearnerContent(Course course, String userId) {
        Optional<EnrolledSnapshot> snapshotOpt = snapshotRepository.findByUserIdAndCourseId(userId, course.getId());

        if (snapshotOpt.isEmpty()) {
            Enrollment enrollment = enrollmentRepository.findByUser_IdAndCourse_Id(userId, course.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.HAVE_NOT_JOIN_THIS_COURSE));

            initializeLazyCollections(course);
            EnrolledSnapshot newSnapshot = snapshotService.createSnapshot(enrollment, course);
            if (newSnapshot != null) {
                snapshotOpt = Optional.of(newSnapshot);
            }
        }

        if (snapshotOpt.isPresent()) {
            return buildResponseFromSnapshot(snapshotOpt.get(), course);
        }

        return buildDetailResponse(course, userId, null);
    }

    @SuppressWarnings("unchecked")
    private CourseDetailResponse buildResponseFromSnapshot(EnrolledSnapshot snapshot, Course course) {
        Map<String, Object> structure = snapshotService.parseStructure(snapshot.getStructureJson());

        List<ChapterSummaryResponse> chapters = buildChaptersFromSnapshot(
                (List<Map<String, Object>>) structure.getOrDefault("chapters", Collections.emptyList()));

        List<TestResponse> courseTests = buildTestsFromSnapshot(
                (List<Map<String, Object>>) structure.getOrDefault("courseTests", Collections.emptyList()));

        return CourseDetailResponse.builder()
                .id(snapshot.getOriginalCourseId())
                .slug(course.getSlug())
                .title(snapshot.getCourseTitle())
                .description(snapshot.getCourseDescription())
                .imageUrl(snapshot.getCourseImageUrl())
                .videoUrl(snapshot.getCourseVideoUrl())
                .instructorName(snapshot.getInstructorName())
                .instructorId(snapshot.getInstructorId())
                .enrolled(true)
                .completed(false)
                .chapters(chapters)
                .courseTests(courseTests)
                .price(course.getPrice())
                .currency(course.getCurrency() != null ? course.getCurrency().name() : null)
                .discount(course.getDiscount())
                .discountEndTime(course.getDiscountEndTime())
                .draft(course.isDraft())
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<ChapterSummaryResponse> buildChaptersFromSnapshot(List<Map<String, Object>> chaptersData) {
        List<ChapterSummaryResponse> chapters = new ArrayList<>();

        for (Map<String, Object> chapterData : chaptersData) {
            List<LectureSummaryResponse> lectures = ((List<Map<String, Object>>) chapterData.getOrDefault("lectures", Collections.emptyList()))
                    .stream()
                    .map(this::buildLectureFromSnapshot)
                    .toList();

            List<TestResponse> tests = buildTestsFromSnapshot(
                    (List<Map<String, Object>>) chapterData.getOrDefault("tests", Collections.emptyList()));

            chapters.add(ChapterSummaryResponse.builder()
                    .id(((Number) chapterData.get("originalId")).longValue())
                    .title((String) chapterData.get("title"))
                    .orderIndex(getIntValue(chapterData, "orderIndex", 0))
                    .lectures(lectures)
                    .tests(tests)
                    .build());
        }
        return chapters;
    }

    private LectureSummaryResponse buildLectureFromSnapshot(Map<String, Object> lectureData) {
        return LectureSummaryResponse.builder()
                .id(((Number) lectureData.get("originalId")).longValue())
                .title((String) lectureData.get("title"))
                .description((String) lectureData.get("description"))
                .videoUrl((String) lectureData.get("videoUrl"))
                .time(getIntValue(lectureData, "duration", 0))
                .orderIndex(getIntValue(lectureData, "orderIndex", 0))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<TestResponse> buildTestsFromSnapshot(List<Map<String, Object>> testsData) {
        List<TestResponse> tests = new ArrayList<>();

        for (Map<String, Object> testData : testsData) {
            List<QuestionResponse> questions = ((List<Map<String, Object>>) testData.getOrDefault("questions", Collections.emptyList()))
                    .stream()
                    .map(this::buildQuestionFromSnapshot)
                    .toList();

            tests.add(TestResponse.builder()
                    .id(((Number) testData.get("originalId")).longValue())
                    .title((String) testData.get("title"))
                    .durationMinutes(getIntValue(testData, "durationMinutes", 0))
                    .passScore(getIntValue(testData, "passScore", 0))
                    .questions(questions)
                    .build());
        }
        return tests;
    }

    @SuppressWarnings("unchecked")
    private QuestionResponse buildQuestionFromSnapshot(Map<String, Object> qData) {
        List<AnswerResponse> answers = ((List<Map<String, Object>>) qData.getOrDefault("answers", Collections.emptyList()))
                .stream()
                .map(aData -> AnswerResponse.builder()
                        .id(((Number) aData.get("id")).longValue())
                        .content((String) aData.get("content"))
                        .correct(Boolean.TRUE.equals(aData.get("correct")))
                        .build())
                .toList();

        return QuestionResponse.builder()
                .id(((Number) qData.get("id")).longValue())
                .content((String) qData.get("content"))
                .explanation((String) qData.get("explanation"))
                .orderIndex(getIntValue(qData, "orderIndex", 0))
                .answers(answers)
                .build();
    }

    private void initializeLazyCollections(Course course) {
        if (course.getChapters() != null) {
            Hibernate.initialize(course.getChapters());
            for (Chapter chapter : course.getChapters()) {
                if (chapter.getLectures() != null) Hibernate.initialize(chapter.getLectures());
                if (chapter.getTests() != null) {
                    Hibernate.initialize(chapter.getTests());
                    chapter.getTests().forEach(this::initializeTestContent);
                }
            }
        }
        if (course.getCourseTests() != null) {
            Hibernate.initialize(course.getCourseTests());
            course.getCourseTests().forEach(this::initializeTestContent);
        }
    }

    private void initializeTestContent(Test test) {
        if (test.getQuestions() != null) {
            Hibernate.initialize(test.getQuestions());
            for (Question q : test.getQuestions()) {
                if (q.getAnswers() != null) Hibernate.initialize(q.getAnswers());
            }
        }
    }

    private void setAverageRating(Course course, CourseDetailResponse response) {
        try {
            List<FeedbackRepository.CourseRatingAgg> aggs = feedbackRepository
                    .aggregateRatingsByCourseIds(List.of(course.getId()), FeedbackStatus.VISIBLE);
            if (aggs != null && !aggs.isEmpty() && aggs.get(0) != null) {
                response.setRating(aggs.get(0).getAvgRating());
            }
        } catch (Exception ignored) {}
    }

    private void hideNonPreviewContent(CourseDetailResponse response) {
        if (response.getChapters() == null) return;
        response.getChapters().forEach(chapter -> {
            if (chapter.getLectures() != null) {
                chapter.getLectures().forEach(lecture -> {
                    if (!Boolean.TRUE.equals(lecture.getPreviewFree())) {
                        lecture.setVideoUrl(null);
                        lecture.setResourceType(null);
                        lecture.setResourceUrl(null);
                    }
                });
            }
        });
    }

    private void setPaymentMethods(Course course, CourseDetailResponse response) {
        if (course.getCoursePaymentMethods() == null) return;
        List<CoursePaymentMethodResponse> methods = course.getCoursePaymentMethods().stream()
                .map(cpm -> CoursePaymentMethodResponse.builder()
                        .id(cpm.getId())
                        .paymentMethod(cpm.getPaymentMethod())
                        .receiverAddress(cpm.getReceiverAddress())
                        .build())
                .toList();
        response.setCoursePaymentMethods(methods);
    }

    private List<CourseSummaryResponse> buildSummariesWithRatings(List<Course> courses) {
        List<CourseSummaryResponse> summaries = courses.stream()
                .map(courseMapper::toSummaryResponse)
                .toList();

        List<String> ids = courses.stream().map(Course::getId).toList();
        if (!ids.isEmpty()) {
            Map<String, Double> ratingMap = feedbackRepository
                    .aggregateRatingsByCourseIds(ids, FeedbackStatus.VISIBLE).stream()
                    .collect(Collectors.toMap(
                            FeedbackRepository.CourseRatingAgg::getCourseId,
                            FeedbackRepository.CourseRatingAgg::getAvgRating));
            summaries.forEach(s -> {
                if (ratingMap.containsKey(s.getId())) s.setRating(ratingMap.get(s.getId()));
            });
        }
        return summaries;
    }

    private PageResponse<CourseSummaryResponse> buildSummaryPageResponse(Page<Course> coursePage) {
        List<CourseSummaryResponse> content = coursePage.getContent().stream()
                .map(course -> {
                    CourseSummaryResponse summary = courseMapper.toSummaryResponse(course);
                    summary.setTotalTime(String.valueOf(CourseDurationCalculator.calculateTotalMinutes(course)));
                    return summary;
                })
                .toList();

        return PageResponse.<CourseSummaryResponse>builder()
                .content(content)
                .page(coursePage.getNumber())
                .size(coursePage.getSize())
                .totalElements(coursePage.getTotalElements())
                .totalPages(coursePage.getTotalPages())
                .build();
    }

    private <T> PageResponse<T> buildPageResponse(List<T> content, Page<?> page) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private Specification<Course> buildSearchSpec(String keyword, CourseType courseType,
            Integer minPrice, Integer maxPrice, String tagId) {
        Specification<Course> spec = Specification.where(isDraft(false));
        if (keyword != null && !keyword.isBlank()) spec = spec.and(titleOrInstructorNameContains(keyword));
        if (courseType != null) spec = spec.and(hasCourseType(courseType));
        if (minPrice != null || maxPrice != null) spec = spec.and(priceBetween(minPrice, maxPrice));
        if (tagId != null && !tagId.isBlank()) spec = spec.and(hasTagId(tagId));
        return spec;
    }

    private List<Course> sortByPopularityIfNeeded(List<Course> courses, String sort) {
        if (!SORT_POPULAR.equalsIgnoreCase(sort)) return new ArrayList<>(courses);
        List<Course> sorted = new ArrayList<>(courses);
        sorted.sort((a, b) -> {
            int enrollA = a.getEnrollments() != null ? a.getEnrollments().size() : 0;
            int enrollB = b.getEnrollments() != null ? b.getEnrollments().size() : 0;
            int cmp = Integer.compare(enrollB, enrollA);
            if (cmp != 0) return cmp;
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return sorted;
    }

    private void validateInstructorOwnership(String instructorId) {
        String userId = getCurrentUserId();
        if (!userId.equals(instructorId)) {
            throw new AppException(ErrorCode.YOU_ARE_NOT_INSTRUCTOR);
        }
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() ? auth.getName() : null;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        return value != null ? ((Number) value).intValue() : defaultValue;
    }
}

