package com.cardano_lms.server.Service;

import com.cardano_lms.server.Entity.VerificationToken;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.VerificationTokenRepository;
import com.cardano_lms.server.Repository.UserRepository;
import com.cardano_lms.server.constant.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();

    public void startReset(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        VerificationToken token = VerificationToken.builder()
                .type(TokenType.PASSWORD_RESET)
                .email(user.getEmail())
                .code(code)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .used(false)
                .build();
        tokenRepository.save(token);
        emailService.sendVerificationCode(user.getEmail(), code);
    }

    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        VerificationToken latest = tokenRepository
                .findTopByEmailAndTypeAndUsedOrderByCreatedAtDesc(user.getEmail(), TokenType.PASSWORD_RESET, false)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (latest.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.CODE_ERROR);
        }
        if (!latest.getCode().equals(code)) {
            latest.setAttempts(latest.getAttempts() + 1);
            tokenRepository.save(latest);
            throw new AppException(ErrorCode.CODE_ERROR);
        }
        tokenRepository.delete(latest);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
