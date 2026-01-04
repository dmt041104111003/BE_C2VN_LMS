package com.cardano_lms.server.Service;

import com.cardano_lms.server.Entity.VerificationToken;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.VerificationTokenRepository;
import com.cardano_lms.server.Repository.UserRepository;
import com.cardano_lms.server.constant.TokenType;
import com.cardano_lms.server.constant.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    public void startVerification(String email) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        VerificationToken token = VerificationToken.builder()
                .type(TokenType.EMAIL_VERIFICATION)
                .email(email)
                .code(code)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .used(false)
                .build();
        tokenRepository.save(token);
        emailService.sendVerificationCode(email, code);
    }

    public void verifyCode(String email, String code) {
        VerificationToken latest = tokenRepository
                .findTopByEmailAndTypeAndUsedOrderByCreatedAtDesc(email, TokenType.EMAIL_VERIFICATION, false)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (latest.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        if (!latest.getCode().equals(code)) {
            latest.setAttempts(latest.getAttempts() + 1);
            tokenRepository.save(latest);
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        tokenRepository.delete(latest);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    public void resend(String email) {
        startVerification(email);
    }
}
