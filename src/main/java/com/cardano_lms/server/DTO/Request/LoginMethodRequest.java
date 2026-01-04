package com.cardano_lms.server.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginMethodRequest {
    String name;
    String description;
}
