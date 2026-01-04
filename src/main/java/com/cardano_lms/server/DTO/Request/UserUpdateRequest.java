package com.cardano_lms.server.DTO.Request;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class UserUpdateRequest {
    String password;
    String email;
    String github;
    String firstName;
    LocalDate dob;
    String lastName;
    String walletAddress;
}
