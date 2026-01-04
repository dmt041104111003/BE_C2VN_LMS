package com.cardano_lms.server.DTO.Response;

import com.cardano_lms.server.constant.UserStatus;
import com.cardano_lms.server.Entity.LoginMethod;
import com.cardano_lms.server.Entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String email;
    String fullName;
    String bio;
    UserStatus status;
    String google;
    String github;
    String walletAddress;
    LoginMethod loginMethod;
    Role role;
    boolean hasPassword;
}

