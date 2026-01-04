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
public class CourseActivityResponse {
    private Long id;
    private String type;
    private String description;
    private String userName;
    private LocalDateTime timestamp;
}


