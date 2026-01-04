package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.NonceCreationRequest;
import com.cardano_lms.server.DTO.Response.NonceCreationResponse;
import com.cardano_lms.server.Entity.Nonce;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NonceMapper {
    NonceCreationResponse toNonceResponse(Nonce nonce);
    Nonce toNonce(NonceCreationRequest nonceRequest);
}
