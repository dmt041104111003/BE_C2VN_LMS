package com.cardano_lms.server.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityResponse {
    String type;
    Long progressId;
    LocalDate completedAt;
}
