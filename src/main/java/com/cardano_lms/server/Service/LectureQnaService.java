package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Request.LectureCommentCreateRequest;
import com.cardano_lms.server.DTO.Request.LectureQnaCreateRequest;
import com.cardano_lms.server.DTO.Request.LectureQnaReplyCreateRequest;
import com.cardano_lms.server.DTO.Response.LectureQnaReplyResponse;
import com.cardano_lms.server.DTO.Response.LectureQnaResponse;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.Lecture;
import com.cardano_lms.server.Entity.LectureComment;
import com.cardano_lms.server.Entity.LectureCommentReaction;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.EnrollmentRepository;
import com.cardano_lms.server.Repository.LectureCommentRepository;
import com.cardano_lms.server.Repository.LectureCommentReactionRepository;
import com.cardano_lms.server.Repository.LectureRepository;
import com.cardano_lms.server.Repository.UserRepository;
import com.cardano_lms.server.Mapper.LectureQnaMapper;
import com.cardano_lms.server.constant.ReactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureQnaService {

    private final LectureCommentRepository commentRepository;
    private final LectureCommentReactionRepository reactionRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final LectureQnaMapper lectureQnaMapper;

    public List<LectureQnaResponse> getQnaByLecture(Long lectureId) {
        Lecture lecture = findLectureOrThrow(lectureId);
        List<LectureComment> all = commentRepository.findByLecture_IdOrderByCreatedAtDesc(lecture.getId());
        boolean canSeeAll = isCurrentUserCourseInstructor(extractCourse(lecture));
        String currentUserId = getCurrentUserIdSafe();

        Map<Long, LectureQnaResponse> map = all.stream().collect(Collectors.toMap(
                LectureComment::getId,
                c -> toResponseWithReaction(c, currentUserId, canSeeAll)));

        List<LectureQnaResponse> roots = new ArrayList<>();
        for (LectureComment c : all) {
            LectureQnaResponse node = map.get(c.getId());
            if (c.getParent() == null) {
                roots.add(node);
            } else if (map.containsKey(c.getParent().getId())) {
                map.get(c.getParent().getId()).getReplies().add(node);
            }
        }
        return roots;
    }
    
    
    public List<LectureQnaResponse> getQnaByCourse(String courseId) {
        
        List<LectureComment> all = commentRepository.findAllByCourseIdIncludingDeleted(courseId);
        String currentUserId = getCurrentUserIdSafe();
        
        Map<Long, LectureQnaResponse> map = all.stream().collect(Collectors.toMap(
                LectureComment::getId,
                c -> toResponseWithReactionAndLecture(c, currentUserId)));

        List<LectureQnaResponse> roots = new ArrayList<>();
        for (LectureComment c : all) {
            LectureQnaResponse node = map.get(c.getId());
            if (c.getParent() == null) {
                roots.add(node);
            } else if (map.containsKey(c.getParent().getId())) {
                map.get(c.getParent().getId()).getReplies().add(node);
            }
        }
        return roots;
    }
    
    private LectureQnaResponse toResponseWithReactionAndLecture(LectureComment c, String currentUserId) {
        int likeCount = (int) reactionRepository.countByCommentIdAndType(c.getId(), ReactionType.LIKE);
        int dislikeCount = (int) reactionRepository.countByCommentIdAndType(c.getId(), ReactionType.DISLIKE);
        
        String userVote = null;
        if (currentUserId != null) {
            Optional<LectureCommentReaction> reaction = reactionRepository.findByUserIdAndCommentId(currentUserId, c.getId());
            userVote = reaction.map(r -> r.getType().name()).orElse(null);
        }
        
        
        Lecture lecture = c.getLecture();
        Long lectureId = lecture != null ? lecture.getId() : null;
        String lectureTitle = lecture != null ? lecture.getTitle() : c.getLectureTitleSnapshot();
        if (lectureTitle == null) {
            lectureTitle = "[Bài giảng đã xóa]";
        }
        
        return LectureQnaResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .userId(c.getUser() != null ? c.getUser().getId() : null)
                .userName(c.getUser() != null ? c.getUser().getFullName() : null)
                .userEmail(c.getUser() != null ? c.getUser().getEmail() : null)
                .userWalletAddress(c.getUser() != null ? c.getUser().getWalletAddress() : null)
                .lectureId(lectureId)
                .lectureTitle(lectureTitle)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .userVote(userVote)
                .visible(c.isVisible())
                .replies(new ArrayList<>())
                .build();
    }
    
    private LectureQnaResponse toResponseWithReaction(LectureComment c, String currentUserId, boolean canSeeAll) {
        int likeCount = (int) reactionRepository.countByCommentIdAndType(c.getId(), ReactionType.LIKE);
        int dislikeCount = (int) reactionRepository.countByCommentIdAndType(c.getId(), ReactionType.DISLIKE);
        
        String userVote = null;
        if (currentUserId != null) {
            Optional<LectureCommentReaction> reaction = reactionRepository.findByUserIdAndCommentId(currentUserId, c.getId());
            userVote = reaction.map(r -> r.getType().name()).orElse(null);
        }
        
        return LectureQnaResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .userId(c.getUser() != null ? c.getUser().getId() : null)
                .userName(c.getUser() != null ? c.getUser().getFullName() : null)
                .userEmail(c.getUser() != null ? c.getUser().getEmail() : null)
                .userWalletAddress(c.getUser() != null ? c.getUser().getWalletAddress() : null)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .userVote(userVote)
                .visible(canSeeAll ? c.isVisible() : true)
                .replies(new ArrayList<>())
                .build();
    }

    public LectureQnaResponse createQuestion(Long lectureId, LectureQnaCreateRequest req) {
        String userId = getCurrentUserId();
        User user = findUserOrThrow(userId);
        Lecture lecture = findLectureOrThrow(lectureId);
        
        Course course = extractCourse(lecture);
        validateCourseExists(course != null ? course.getId() : null);
        validateUserCanComment(userId, course);
        
        LectureComment c = LectureComment.builder()
                .lecture(lecture)
                .lectureTitleSnapshot(lecture.getTitle())
                .courseId(course.getId()) 
                .user(user)
                .content(req.getContent())
                .createdAt(LocalDateTime.now())
                .visible(true)
                .likeCount(0)
                .build();
        return toResponse(commentRepository.save(c));
    }

    public LectureQnaReplyResponse reply(Long parentId, LectureQnaReplyCreateRequest req) {
        String userId = getCurrentUserId();
        User user = findUserOrThrow(userId);
        LectureComment parent = findCommentOrThrow(parentId);
        Lecture lecture = parent.getLecture();
        
        
        Course course = extractCourse(lecture);
        if (course != null) {
            validateUserCanComment(userId, course);
        }
        
        
        String lectureTitle = lecture != null ? lecture.getTitle() : parent.getLectureTitleSnapshot();
        String courseId = course != null ? course.getId() : parent.getCourseId();
        
        LectureComment child = LectureComment.builder()
                .lecture(lecture)
                .lectureTitleSnapshot(lectureTitle)
                .courseId(courseId)
                .user(user)
                .parent(parent)
                .content(req.getContent())
                .createdAt(LocalDateTime.now())
                .visible(true)
                .likeCount(0)
                .build();
        LectureComment saved = commentRepository.save(child);

        return LectureQnaReplyResponse.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .userId(user.getId())
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .userWalletAddress(user.getWalletAddress())
                .build();
    }

    public LectureQnaResponse update(Long commentId, LectureCommentCreateRequest req) {
        String userId = getCurrentUserId();
        LectureComment c = findCommentOrThrow(commentId);
        
        if (!Objects.equals(extractUserId(c), userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        c.setContent(req.getContent());
        c.setUpdatedAt(LocalDateTime.now());
        return toResponse(commentRepository.save(c));
    }

    public void delete(Long commentId) {
        String userId = getCurrentUserId();
        LectureComment c = findCommentOrThrow(commentId);
        
        boolean isOwner = Objects.equals(extractUserId(c), userId);
        boolean isInstructor = isCurrentUserCommentCourseInstructor(c);
        
        if (!isOwner && !isInstructor) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        commentRepository.delete(c);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public LectureQnaResponse hide(Long commentId) {
        LectureComment c = findCommentOrThrow(commentId);
        validateCurrentUserIsCommentCourseInstructor(c);
        c.setVisible(false);
        return lectureQnaMapper.toResponseForInstructor(commentRepository.save(c));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public LectureQnaResponse unhide(Long commentId) {
        LectureComment c = findCommentOrThrow(commentId);
        validateCurrentUserIsCommentCourseInstructor(c);
        c.setVisible(true);
        return lectureQnaMapper.toResponseForInstructor(commentRepository.save(c));
    }

    @Transactional
    public LectureQnaResponse react(Long commentId, ReactionType type) {
        String userId = getCurrentUserId();
        User user = findUserOrThrow(userId);
        LectureComment comment = findCommentOrThrow(commentId);
        
        Optional<LectureCommentReaction> existing = reactionRepository.findByUserIdAndCommentId(userId, commentId);
        
        if (existing.isPresent()) {
            LectureCommentReaction reaction = existing.get();
            if (reaction.getType() == type) {
                
                reactionRepository.delete(reaction);
            } else {
                
                reaction.setType(type);
                reaction.setCreatedAt(LocalDateTime.now());
                reactionRepository.save(reaction);
            }
        } else {
            
            LectureCommentReaction reaction = LectureCommentReaction.builder()
                    .user(user)
                    .comment(comment)
                    .type(type)
                    .createdAt(LocalDateTime.now())
                    .build();
            reactionRepository.save(reaction);
        }
        
        return toResponseWithReaction(comment, userId, false);
    }
    
    @Transactional
    public LectureQnaResponse like(Long commentId) {
        return react(commentId, ReactionType.LIKE);
    }

    @Transactional
    public LectureQnaResponse dislike(Long commentId) {
        return react(commentId, ReactionType.DISLIKE);
    }
    
    @Transactional
    public LectureQnaResponse removeReaction(Long commentId) {
        String userId = getCurrentUserId();
        LectureComment comment = findCommentOrThrow(commentId);
        
        reactionRepository.findByUserIdAndCommentId(userId, commentId)
                .ifPresent(reactionRepository::delete);
        
        return toResponseWithReaction(comment, userId, false);
    }

    private Lecture findLectureOrThrow(Long lectureId) {
        return lectureRepository.findById(lectureId).orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    }

    private LectureComment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private User findUserOrThrow(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private LectureQnaResponse toResponse(LectureComment c) {
        return lectureQnaMapper.toResponse(c);
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return auth.getName();
    }
    
    private String getCurrentUserIdSafe() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {
            
        }
        return null;
    }

    private Course extractCourse(Lecture lecture) {
        if (lecture == null || lecture.getChapter() == null) return null;
        return lecture.getChapter().getCourse();
    }

    private String extractCourseId(Lecture lecture) {
        Course course = extractCourse(lecture);
        return course != null ? course.getId() : null;
    }

    private String extractInstructorId(Course course) {
        return course != null && course.getInstructor() != null ? course.getInstructor().getId() : null;
    }

    private String extractUserId(LectureComment c) {
        return c.getUser() != null ? c.getUser().getId() : null;
    }

    private Course extractCourseFromComment(LectureComment c) {
        if (c.getLecture() == null) return null;
        return extractCourse(c.getLecture());
    }

    private boolean isCurrentUserCourseInstructor(Course course) {
        try {
            String currentUserId = getCurrentUserId();
            String instructorId = extractInstructorId(course);
            return instructorId != null && instructorId.equals(currentUserId);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isCurrentUserCommentCourseInstructor(LectureComment c) {
        return isCurrentUserCourseInstructor(extractCourseFromComment(c));
    }

    private void validateCurrentUserIsCommentCourseInstructor(LectureComment c) {
        if (!isCurrentUserCommentCourseInstructor(c)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateCourseExists(String courseId) {
        if (courseId == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateUserEnrolled(String userId, String courseId) {
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new AppException(ErrorCode.HAVE_NOT_JOIN_THIS_COURSE);
        }
    }

    private void validateUserCanComment(String userId, Course course) {
        if (course == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        
        
        String instructorId = extractInstructorId(course);
        if (instructorId != null && instructorId.equals(userId)) {
            return;
        }
        
        
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId())) {
            return;
        }
        
        throw new AppException(ErrorCode.HAVE_NOT_JOIN_THIS_COURSE);
    }
}
