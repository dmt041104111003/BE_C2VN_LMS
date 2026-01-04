package com.cardano_lms.server.Service;

import com.cardano_lms.server.constant.FeedbackStatus;
import com.cardano_lms.server.constant.ReactionType;
import com.cardano_lms.server.DTO.Request.FeedbackRequest;
import com.cardano_lms.server.DTO.Response.FeedbackResponse;
import com.cardano_lms.server.DTO.Response.PageResponse;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.Feedback;
import com.cardano_lms.server.Entity.FeedbackReaction;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.CourseRepository;
import com.cardano_lms.server.Repository.EnrollmentRepository;
import com.cardano_lms.server.Repository.FeedbackReactionRepository;
import com.cardano_lms.server.Repository.FeedbackRepository;
import com.cardano_lms.server.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Comparator;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import com.cardano_lms.server.DTO.Response.InboxItemResponse;
import com.cardano_lms.server.Entity.LectureComment;
import com.cardano_lms.server.Entity.InboxReadStatus;
import com.cardano_lms.server.Entity.Certificate;
import com.cardano_lms.server.Repository.LectureCommentRepository;
import com.cardano_lms.server.Repository.InboxReadStatusRepository;
import com.cardano_lms.server.Repository.CertificateRepository;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FeedbackReactionRepository reactionRepository;
    private final LectureCommentRepository lectureCommentRepository;
    private final InboxReadStatusRepository inboxReadStatusRepository;
    private final CertificateRepository certificateRepository;

    private static final List<FeedbackStatus> INSTRUCTOR_VISIBLE_STATUSES = 
            Arrays.asList(FeedbackStatus.VISIBLE, FeedbackStatus.HIDDEN);

    public List<FeedbackResponse> getFeedbacksByCourse(String courseId, boolean isAdmin) {
        Course course = findCourseOrThrow(courseId);
        boolean canSeeAll = isCurrentUserCourseInstructor(course);
        String currentUserId = getCurrentUserIdSafe();
        
        List<Feedback> feedbacks = canSeeAll
                ? feedbackRepository.findByCourseIdAndParentIsNullAndStatusInOrderByCreatedAtDesc(courseId, INSTRUCTOR_VISIBLE_STATUSES)
                : feedbackRepository.findByCourseIdAndParentIsNullAndStatusOrderByCreatedAtDesc(courseId, FeedbackStatus.VISIBLE);

        return feedbacks.stream()
                .map(f -> toFeedbackResponse(f, currentUserId))
                .collect(Collectors.toList());
    }

    public PageResponse<FeedbackResponse> getFeedbacksByCoursePaged(String courseId, int page, int size) {
        Course course = findCourseOrThrow(courseId);
        boolean canSeeAll = isCurrentUserCourseInstructor(course);
        String currentUserId = getCurrentUserIdSafe();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Feedback> result = canSeeAll
                ? feedbackRepository.findByCourseIdAndParentIsNullAndStatusInOrderByCreatedAtDesc(courseId, INSTRUCTOR_VISIBLE_STATUSES, pageable)
                : feedbackRepository.findByCourseIdAndParentIsNullAndStatusOrderByCreatedAtDesc(courseId, FeedbackStatus.VISIBLE, pageable);

        return PageResponse.<FeedbackResponse>builder()
                .content(result.getContent().stream()
                        .map(f -> toFeedbackResponse(f, currentUserId))
                        .toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private FeedbackResponse toFeedbackResponse(Feedback feedback, String currentUserId) {
        long likeCount = reactionRepository.countByFeedbackIdAndType(feedback.getId(), ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByFeedbackIdAndType(feedback.getId(), ReactionType.DISLIKE);
        
        String userReaction = currentUserId != null
                ? reactionRepository.findByUserIdAndFeedbackId(currentUserId, feedback.getId())
                        .map(r -> r.getType().name()).orElse(null)
                : null;

        List<FeedbackResponse> replies = (feedback.getReplies() != null && !feedback.getReplies().isEmpty())
                ? feedback.getReplies().stream()
                        .map(r -> toFeedbackResponse(r, currentUserId))
                        .collect(Collectors.toList())
                : null;

        User user = feedback.getUser();
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .rate(feedback.getRate())
                .content(feedback.getContent())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .userId(user != null ? user.getId() : null)
                .fullName(user != null ? user.getFullName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userWalletAddress(user != null ? user.getWalletAddress() : null)
                .status(feedback.getStatus())
                .parentId(feedback.getParent() != null ? feedback.getParent().getId() : null)
                .replies(replies)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .userReaction(userReaction)
                .build();
    }

    @Transactional
    public FeedbackResponse addFeedback(String courseId, FeedbackRequest request) {
        String userId = getCurrentUserId();
        User user = findUserOrThrow(userId);
        Course course = findCourseOrThrow(courseId);
        
        if (request.getParentId() != null) {
            return addReply(courseId, request, user, course);
        }
        
        validateUserEnrolled(userId, courseId);
        validateNoDuplicateFeedback(userId, courseId);

        Feedback feedback = Feedback.builder()
                .rate(request.getRate())
                .content(request.getContent())
                .user(user)
                .course(course)
                .createdAt(LocalDateTime.now())
                .status(FeedbackStatus.VISIBLE)
                .build();

        return toFeedbackResponse(feedbackRepository.save(feedback), userId);
    }

    private FeedbackResponse addReply(String courseId, FeedbackRequest request, User user, Course course) {
        Feedback parent = findFeedbackOrThrow(request.getParentId());
        
        if (!Objects.equals(parent.getCourse().getId(), courseId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        Feedback rootFeedback = parent.getParent() != null ? parent.getParent() : parent;
        String originalCommenterId = rootFeedback.getUser().getId();
        String instructorId = course.getInstructor().getId();
        String currentUserId = user.getId();
        
        boolean canReply = currentUserId.equals(instructorId) || currentUserId.equals(originalCommenterId);
        if (!canReply) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        Feedback reply = Feedback.builder()
                .content(request.getContent())
                .user(user)
                .course(course)
                .parent(rootFeedback)
                .createdAt(LocalDateTime.now())
                .status(FeedbackStatus.VISIBLE)
                .build();

        return toFeedbackResponse(feedbackRepository.save(reply), currentUserId);
    }

    @Transactional
    public FeedbackResponse updateFeedback(Long feedbackId, FeedbackRequest request) {
        String userId = getCurrentUserId();
        Feedback feedback = findFeedbackOrThrow(feedbackId);
        
        if (!Objects.equals(feedback.getUser().getId(), userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            feedback.setContent(request.getContent().trim());
        }
        
        if (request.getRate() != null && feedback.getParent() == null) {
            feedback.setRate(request.getRate());
        }
        
        feedback.setUpdatedAt(LocalDateTime.now());
        return toFeedbackResponse(feedbackRepository.save(feedback), userId);
    }

    @Transactional
    public FeedbackResponse reactToFeedback(Long feedbackId, String reactionType) {
        String userId = getCurrentUserId();
        User user = findUserOrThrow(userId);
        Feedback feedback = findFeedbackOrThrow(feedbackId);
        
        ReactionType type = ReactionType.valueOf(reactionType.toUpperCase());
        Optional<FeedbackReaction> existingReaction = reactionRepository.findByUserIdAndFeedbackId(userId, feedbackId);
        
        if (existingReaction.isPresent()) {
            FeedbackReaction reaction = existingReaction.get();
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
            } else {
                reaction.setType(type);
                reactionRepository.save(reaction);
            }
        } else {
            FeedbackReaction newReaction = FeedbackReaction.builder()
                    .user(user)
                    .feedback(feedback)
                    .type(type)
                    .createdAt(LocalDateTime.now())
                    .build();
            reactionRepository.save(newReaction);
        }
        
        return toFeedbackResponse(feedback, userId);
    }

    @Transactional
    public void deleteFeedback(Long feedbackId) {
        Feedback fb = findFeedbackOrThrow(feedbackId);
        String currentUserId = getCurrentUserId();
        
        boolean isOwner = Objects.equals(currentUserId, extractUserId(fb));
        boolean isInstructor = isCurrentUserFeedbackCourseInstructor(fb);

        if (!isOwner && !isInstructor) {
            throw new AppException(ErrorCode.DONT_DELETE_OUR_FEEDBACK);
        }
        
        feedbackRepository.delete(fb);
    }
    
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getMyFeedbacks() {
        String userId = getCurrentUserId();
        List<Feedback> feedbacks = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return feedbacks.stream()
                .map(f -> toFeedbackResponseWithCourse(f, userId))
                .collect(Collectors.toList());
    }
    
    private FeedbackResponse toFeedbackResponseWithCourse(Feedback feedback, String currentUserId) {
        FeedbackResponse response = toFeedbackResponse(feedback, currentUserId);
        if (feedback.getCourse() != null) {
            response.setCourseTitle(feedback.getCourse().getTitle());
            response.setCourseSlug(feedback.getCourse().getSlug());
        }
        return response;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void hideFeedback(Long feedbackId) {
        Feedback fb = findFeedbackOrThrow(feedbackId);
        validateCurrentUserIsCourseInstructor(fb);
        fb.setStatus(FeedbackStatus.HIDDEN);
        feedbackRepository.save(fb);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void unHideFeedback(Long feedbackId) {
        Feedback fb = findFeedbackOrThrow(feedbackId);
        validateCurrentUserIsCourseInstructor(fb);
        fb.setStatus(FeedbackStatus.VISIBLE);
        feedbackRepository.save(fb);
    }

    private Course findCourseOrThrow(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    }

    private Feedback findFeedbackOrThrow(Long feedbackId) {
        return feedbackRepository.findByIdWithReplies(feedbackId)
                .orElseThrow(() -> new AppException(ErrorCode.FEEDBACK_NOT_FOUND));
    }

    private User findUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String getCurrentUserIdSafe() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractInstructorId(Course course) {
        return course != null && course.getInstructor() != null ? course.getInstructor().getId() : null;
    }

    private String extractUserId(Feedback fb) {
        return fb.getUser() != null ? fb.getUser().getId() : null;
    }

    private boolean isCurrentUserCourseInstructor(Course course) {
        String currentUserId = getCurrentUserIdSafe();
        String instructorId = extractInstructorId(course);
        return instructorId != null && instructorId.equals(currentUserId);
    }

    private boolean isCurrentUserFeedbackCourseInstructor(Feedback fb) {
        return fb.getCourse() != null && isCurrentUserCourseInstructor(fb.getCourse());
    }

    private void validateCurrentUserIsCourseInstructor(Feedback fb) {
        if (!isCurrentUserFeedbackCourseInstructor(fb)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateUserEnrolled(String userId, String courseId) {
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new AppException(ErrorCode.HAVE_NOT_JOIN_THIS_COURSE);
        }
    }

    private void validateNoDuplicateFeedback(String userId, String courseId) {
        if (feedbackRepository.existsByCourseIdAndUserIdAndParentIsNull(courseId, userId)) {
            throw new AppException(ErrorCode.ALREADY_FEEDBACK);
        }
    }
    
    public List<InboxItemResponse> getMyInbox() {
        String userId = getCurrentUserId();
        
        Set<Long> readFeedbackIds = inboxReadStatusRepository.findReadItemIdsByUserIdAndItemType(userId, "FEEDBACK");
        Set<Long> readCommentIds = inboxReadStatusRepository.findReadItemIdsByUserIdAndItemType(userId, "LECTURE_COMMENT");
        Set<Long> readCertIds = inboxReadStatusRepository.findReadItemIdsByUserIdAndItemType(userId, "CERTIFICATE");
        
        List<InboxItemResponse> allItems = new ArrayList<>();
        allItems.addAll(collectFeedbackInboxItems(userId, readFeedbackIds));
        allItems.addAll(collectCommentInboxItems(userId, readCommentIds));
        allItems.addAll(collectCertificateInboxItems(userId, readCertIds));
        
        allItems.sort(Comparator.comparing(InboxItemResponse::getCreatedAt).reversed());
        return allItems;
    }
    
    private List<InboxItemResponse> collectCertificateInboxItems(String userId, Set<Long> readCertIds) {
        return certificateRepository.findByEnrollment_User_Id(userId).stream()
                .map(c -> toInboxItem(c, readCertIds.contains(c.getId())))
                .collect(Collectors.toList());
    }
    
    private InboxItemResponse toInboxItem(Certificate cert, boolean isRead) {
        var enrollment = cert.getEnrollment();
        var course = enrollment != null ? enrollment.getCourse() : null;
        var user = enrollment != null ? enrollment.getUser() : null;
        
        return InboxItemResponse.builder()
                .id(cert.getId())
                .type("CERTIFICATE")
                .content("Chúc mừng! Bạn đã hoàn thành khóa học và được cấp chứng chỉ NFT.")
                .rate(null)
                .createdAt(cert.getIssuedAt())
                .courseId(course != null ? course.getId() : null)
                .courseTitle(course != null ? course.getTitle() : null)
                .courseSlug(course != null ? course.getSlug() : null)
                .userId(user != null ? user.getId() : null)
                .userName(user != null ? user.getFullName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userWalletAddress(enrollment != null ? enrollment.getWalletAddress() : null)
                .lectureId(null)
                .lectureTitle(null)
                .parentId(null)
                .isOwn(true)
                .isRead(isRead)
                .certificateId(cert.getId())
                .imgUrl(cert.getImgUrl())
                .build();
    }
    
    private List<InboxItemResponse> collectFeedbackInboxItems(String userId, Set<Long> readFeedbackIds) {
        List<Feedback> allFeedbacks = Stream.of(
                feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId).stream(),
                feedbackRepository.findByInstructorCoursesOrderByCreatedAtDesc(userId).stream(),
                feedbackRepository.findRepliesToUserFeedbacks(userId).stream()
        ).flatMap(s -> s).distinct().collect(Collectors.toList());
        
        return allFeedbacks.stream()
                .map(f -> toInboxItem(f, userId, readFeedbackIds.contains(f.getId())))
                .collect(Collectors.toList());
    }
    
    private List<InboxItemResponse> collectCommentInboxItems(String userId, Set<Long> readCommentIds) {
        List<LectureComment> allComments = Stream.of(
                lectureCommentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream(),
                lectureCommentRepository.findByInstructorCoursesOrderByCreatedAtDesc(userId).stream(),
                lectureCommentRepository.findRepliesToUserComments(userId).stream()
        ).flatMap(s -> s).distinct().collect(Collectors.toList());
        
        return allComments.stream()
                .map(c -> toInboxItem(c, userId, readCommentIds.contains(c.getId())))
                .collect(Collectors.toList());
    }
    
    public long countUnreadInboxItems() {
        String userId = getCurrentUserId();
        
        Set<Long> readFeedbackIds = inboxReadStatusRepository.findReadItemIdsByUserIdAndItemType(userId, "FEEDBACK");
        Set<Long> readCommentIds = inboxReadStatusRepository.findReadItemIdsByUserIdAndItemType(userId, "LECTURE_COMMENT");
        Set<Long> readCertIds = inboxReadStatusRepository.findReadItemIdsByUserIdAndItemType(userId, "CERTIFICATE");
        
        long unreadFeedbacks = countUnreadFeedbacks(userId, readFeedbackIds);
        long unreadComments = countUnreadComments(userId, readCommentIds);
        long unreadCerts = countUnreadCertificates(userId, readCertIds);
        
        return unreadFeedbacks + unreadComments + unreadCerts;
    }
    
    private long countUnreadCertificates(String userId, Set<Long> readCertIds) {
        return certificateRepository.findByEnrollment_User_Id(userId).stream()
                .filter(c -> !readCertIds.contains(c.getId()))
                .count();
    }
    
    private long countUnreadFeedbacks(String userId, Set<Long> readFeedbackIds) {
        long fromCourses = feedbackRepository.findByInstructorCoursesOrderByCreatedAtDesc(userId).stream()
                .filter(f -> f.getUser() == null || !f.getUser().getId().equals(userId))
                .filter(f -> !readFeedbackIds.contains(f.getId()))
                .count();
        
        long fromReplies = feedbackRepository.findRepliesToUserFeedbacks(userId).stream()
                .filter(f -> !readFeedbackIds.contains(f.getId()))
                .count();
        
        return fromCourses + fromReplies;
    }
    
    private long countUnreadComments(String userId, Set<Long> readCommentIds) {
        long fromCourses = lectureCommentRepository.findByInstructorCoursesOrderByCreatedAtDesc(userId).stream()
                .filter(c -> c.getUser() == null || !c.getUser().getId().equals(userId))
                .filter(c -> !readCommentIds.contains(c.getId()))
                .count();
        
        long fromReplies = lectureCommentRepository.findRepliesToUserComments(userId).stream()
                .filter(c -> !readCommentIds.contains(c.getId()))
                .count();
        
        return fromCourses + fromReplies;
    }
    
    @Transactional
    public void markAsRead(String itemType, Long itemId) {
        String userId = getCurrentUserId();
        
        if (inboxReadStatusRepository.existsByUserIdAndItemTypeAndItemId(userId, itemType, itemId)) {
            return;
        }
        
        User user = findUserOrThrow(userId);
        InboxReadStatus readStatus = InboxReadStatus.builder()
                .user(user)
                .itemType(itemType)
                .itemId(itemId)
                .readAt(LocalDateTime.now())
                .build();
        
        inboxReadStatusRepository.save(readStatus);
    }
    
    private InboxItemResponse toInboxItem(Feedback feedback, String currentUserId, boolean isRead) {
        String type = feedback.getParent() != null ? "REVIEW_REPLY" : "REVIEW";
        User user = feedback.getUser();
        Course course = feedback.getCourse();
        boolean isOwn = user != null && user.getId().equals(currentUserId);
        
        return InboxItemResponse.builder()
                .id(feedback.getId())
                .type(type)
                .content(feedback.getContent())
                .rate(feedback.getRate())
                .createdAt(feedback.getCreatedAt())
                .courseId(course != null ? course.getId() : null)
                .courseTitle(course != null ? course.getTitle() : null)
                .courseSlug(course != null ? course.getSlug() : null)
                .userId(user != null ? user.getId() : null)
                .userName(user != null ? user.getFullName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userWalletAddress(user != null ? user.getWalletAddress() : null)
                .parentId(feedback.getParent() != null ? feedback.getParent().getId() : null)
                .isOwn(isOwn)
                .isRead(isOwn || isRead)
                .build();
    }
    
    private InboxItemResponse toInboxItem(LectureComment comment, String currentUserId, boolean isRead) {
        String type = comment.getParent() != null ? "QNA_REPLY" : "QNA";
        User user = comment.getUser();
        boolean isOwn = user != null && user.getId().equals(currentUserId);
        
        String courseId = null;
        String courseTitle = null;
        String courseSlug = null;
        Long lectureId = null;
        String lectureTitle = null;
        
        if (comment.getLecture() != null) {
            lectureId = comment.getLecture().getId();
            lectureTitle = comment.getLecture().getTitle();
            
            if (comment.getLecture().getChapter() != null && comment.getLecture().getChapter().getCourse() != null) {
                Course course = comment.getLecture().getChapter().getCourse();
                courseId = course.getId();
                courseTitle = course.getTitle();
                courseSlug = course.getSlug();
            }
        }
        
        return InboxItemResponse.builder()
                .id(comment.getId())
                .type(type)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .courseId(courseId)
                .courseTitle(courseTitle)
                .courseSlug(courseSlug)
                .lectureId(lectureId)
                .lectureTitle(lectureTitle)
                .userId(user != null ? user.getId() : null)
                .userName(user != null ? user.getFullName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userWalletAddress(user != null ? user.getWalletAddress() : null)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .isOwn(isOwn)
                .isRead(isOwn || isRead)
                .build();
    }
}
