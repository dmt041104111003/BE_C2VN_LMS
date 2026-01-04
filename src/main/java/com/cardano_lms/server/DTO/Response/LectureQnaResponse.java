package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LectureQnaResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;

    private String userId;
    private String userName;
    private String userEmail;
    private String userWalletAddress;
    
    
    private Long lectureId;
    private String lectureTitle;

    private List<LectureQnaResponse> replies;

    private int likeCount;
    private int dislikeCount;
    private String userVote; 

    private boolean visible;
}
