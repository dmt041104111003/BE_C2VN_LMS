package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Response.NonceCreationResponse;
import com.cardano_lms.server.Entity.Nonce;
import com.cardano_lms.server.Entity.Role;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.NonceMapper;
import com.cardano_lms.server.Repository.NonceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.cip30.AddressFormat;
import org.cardanofoundation.cip30.CIP30Verifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NonceService {
    NonceRepository nonceRepository;
    NonceMapper nonceMapper;

    public NonceCreationResponse generateNonce(String address) {
        if(address == null || address.isEmpty())
            throw new AppException(ErrorCode.MISSING_CREDENTIALS);
        String nonce = UUID.randomUUID().toString() + " Authenticate with Cardano2VN";
        Nonce newNonce =  nonceRepository.save(
                Nonce.builder()
                        .address(address)
                        .nonce(nonce)
                        .build()
        );
        return nonceMapper.toNonceResponse(newNonce);
    }

    @Transactional
    public Boolean validateNonce(String nonce,
                                 String signature,String key) {
        Boolean isExistNonce = nonceRepository.existsByNonce(nonce);

        if(!isExistNonce || nonce == null)  {
            throw new AppException(ErrorCode.NONCE_NOT_EXISTED);
        }
        try {
            CIP30Verifier verifier = new CIP30Verifier(signature,key);
            var verificationResult = verifier.verify();
            if(verificationResult.isValid()) {
                
                try {
                    String trimmed = nonce == null ? null : nonce.trim();
                    Optional<Nonce> found = trimmed == null ? Optional.empty() : nonceRepository.findByNonce(trimmed);
                    if (found.isPresent()) {
                        nonceRepository.delete(found.get());
                    } else {
                        nonceRepository.deleteByNonce(trimmed);
                    }
                } catch (Exception ignored) {
                }
                return true;
            }
            else return false;
        } catch (Exception e) {
            throw new RuntimeException("Verification error: " + e.getMessage(), e);
        }
    }

    public void deleteNonce(Long id) {
        nonceRepository.deleteById(id);
    }
}
