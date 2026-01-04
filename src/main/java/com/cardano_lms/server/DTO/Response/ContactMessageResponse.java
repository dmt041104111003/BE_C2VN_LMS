package com.cardano_lms.server.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ContactMessageResponse {
    private Long id;
    private String email;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}
