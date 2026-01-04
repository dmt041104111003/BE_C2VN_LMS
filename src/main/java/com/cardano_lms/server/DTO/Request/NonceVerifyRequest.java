package com.cardano_lms.server.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NonceVerifyRequest {
    String address;
    String nonce;
    String signature;
    String key;
}
