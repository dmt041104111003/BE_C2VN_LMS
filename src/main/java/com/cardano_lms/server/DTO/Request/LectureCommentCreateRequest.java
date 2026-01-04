package com.cardano_lms.server.DTO.Request;

import lombok.Data;

@Data
public class LectureCommentCreateRequest {
    private String content;
    private Long parentId;
}
