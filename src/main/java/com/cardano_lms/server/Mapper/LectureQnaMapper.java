package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Response.LectureQnaResponse;
import com.cardano_lms.server.Entity.LectureComment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class LectureQnaMapper {

    public LectureQnaResponse toResponse(LectureComment comment) {
        if (comment == null)
            return null;
        return LectureQnaResponse.builder()
                .id(comment.getId())
                .content(comment.isVisible() ? comment.getContent() : "")
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .userName(comment.getUser() != null ? comment.getUser().getFullName() : null)
                .userEmail(comment.getUser() != null ? comment.getUser().getEmail() : null)
                .userWalletAddress(comment.getUser() != null ? comment.getUser().getWalletAddress() : null)
                .replies(new ArrayList<>())
                .likeCount(comment.getLikeCount())
                .visible(comment.isVisible())
                .build();
    }

    public LectureQnaResponse toResponseForInstructor(LectureComment comment) {
        if (comment == null)
            return null;
        return LectureQnaResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .userName(comment.getUser() != null ? comment.getUser().getFullName() : null)
                .userEmail(comment.getUser() != null ? comment.getUser().getEmail() : null)
                .userWalletAddress(comment.getUser() != null ? comment.getUser().getWalletAddress() : null)
                .replies(new ArrayList<>())
                .likeCount(comment.getLikeCount())
                .visible(comment.isVisible())
                .build();
    }
}
