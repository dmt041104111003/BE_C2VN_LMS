package com.cardano_lms.server.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    String email;
    String code;
    String newPassword;
}
