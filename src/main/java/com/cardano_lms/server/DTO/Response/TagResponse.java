package com.cardano_lms.server.DTO.Response;



import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagResponse {
    private Long id;
    private String name;
    private String slug;
    private LocalDateTime createdAt;
    private Integer numOfCourses;
}
