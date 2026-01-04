package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboxItemResponse {
    private Long id;
    private String type; 
    private String content;
    private Integer rate; 
    private LocalDateTime createdAt;
    
    
    private String courseId;
    private String courseTitle;
    private String courseSlug;
    
    
    private Long lectureId;
    private String lectureTitle;
    
    
    private String userId;
    private String userName;
    private String userEmail;
    private String userWalletAddress;
    
    
    private Long parentId;
    
    private boolean isOwn;
    private boolean isRead;
    private Long certificateId;
    private String imgUrl;
}

