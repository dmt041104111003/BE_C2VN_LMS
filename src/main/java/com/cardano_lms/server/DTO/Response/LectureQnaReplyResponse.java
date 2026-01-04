package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class LectureQnaReplyResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;

    private String userId;
    private String userName;
    private String userEmail;
    private String userWalletAddress;
}
