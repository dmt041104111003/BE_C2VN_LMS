package com.cardano_lms.server.DTO.Request;

import com.cardano_lms.server.Validator.DobConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRoleRequest {










    String role;
}
