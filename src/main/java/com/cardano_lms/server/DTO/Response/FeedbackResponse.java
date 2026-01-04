package com.cardano_lms.server.DTO.Response;

import com.cardano_lms.server.constant.FeedbackStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    private Long id;
    private Integer rate;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;
    private String fullName;
    private String userEmail;
    private String userWalletAddress;
    private FeedbackStatus status;
    private Long parentId;
    private List<FeedbackResponse> replies;
    private Long likeCount;
    private Long dislikeCount;
    private String userReaction;
    private String courseTitle;
    private String courseSlug;
}
