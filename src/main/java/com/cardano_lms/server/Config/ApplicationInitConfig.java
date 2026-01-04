package com.cardano_lms.server.Config;

import com.cardano_lms.server.constant.*;

import com.cardano_lms.server.Entity.*;
import com.cardano_lms.server.Entity.Role;
import com.cardano_lms.server.constant.MediaType;
import com.cardano_lms.server.Repository.*;
import com.cardano_lms.server.Service.MediaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    MediaService mediaService;

    @NonFinal
    static final String ADMIN_EMAIL = "admin";
    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        LoginMethodRepository loginMethodRepository,
                                        PaymentMethodRepository paymentMethodRepository,
                                        MediaRepository mediaRepository) {
        return args -> {

            if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                roleRepository.save(Role.builder()
                        .name(PredefinedRole.INSTRUCTOR_ROLE)
                        .description("Instructor role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                User user = User.builder()
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role(adminRole)
                        .status(UserStatus.ACTIVE)
                        .build();

                userRepository.save(user);
            }

            if (!loginMethodRepository.existsByName(PredefineLoginMethod.EMAIL_PASSWORD_METHOD)) {
                loginMethodRepository.save(LoginMethod.builder()
                        .name(PredefineLoginMethod.EMAIL_PASSWORD_METHOD)
                        .description("Login with username & password")
                        .build());
            }
            if (!loginMethodRepository.existsByName(PredefineLoginMethod.GOOGLE_METHOD)) {
                loginMethodRepository.save(LoginMethod.builder()
                        .name(PredefineLoginMethod.GOOGLE_METHOD)
                        .description("Login with Google account")
                        .build());
            }
            if (!loginMethodRepository.existsByName(PredefineLoginMethod.GITHUB_METHOD)) {
                loginMethodRepository.save(LoginMethod.builder()
                        .name(PredefineLoginMethod.GITHUB_METHOD)
                        .description("Login with Github account")
                        .build());
            }
            if (!loginMethodRepository.existsByName(PredefineLoginMethod.WALLET_METHOD)) {
                loginMethodRepository.save(LoginMethod.builder()
                        .name(PredefineLoginMethod.WALLET_METHOD)
                        .description("Login with blockchain wallet")
                        .build());
            }

            if (!paymentMethodRepository.existsByName(PredefinedPaymentMethod.CARDANO_WALLET)) {
                paymentMethodRepository.save(PaymentMethod.builder()
                        .name(PredefinedPaymentMethod.CARDANO_WALLET)
                        .description("Payment with cardano wallet")
                        .currency("ADA")
                        .build());
            }
            if (!paymentMethodRepository.existsByName(PredefinedPaymentMethod.FREE_ENROLL)) {
                paymentMethodRepository.save(PaymentMethod.builder()
                        .name(PredefinedPaymentMethod.FREE_ENROLL)
                        .description("Enroll course with free enrollment method")
                        .currency("NO CURRENCY")
                        .build());
            }

            if (mediaRepository.findByType(MediaType.EVENTIMG).isEmpty()) {
                String[] fileNames = {
                        "event-1.jpg",
                        "event-2.jpg",
                        "event-3.jpg",
                        "event-4.jpg",
                        "event-5.jpg",
                        "event-6.png"
                };

                for (int i = 0; i < fileNames.length; i++) {
                    try {
                        ClassPathResource resource = new ClassPathResource("static/events/" + fileNames[i]);
                        byte[] bytes = resource.getInputStream().readAllBytes();

                        mediaService.uploadFromBytes(
                                bytes,
                                i+1,
                                MediaType.EVENTIMG,
                                "Event " + (i + 1),
                                "Mô tả sự kiện " + (i + 1),
                                "Hà Nội, Việt Nam",
                                ""
                        );
                    } catch (IOException ignored) {
                    }
                }
            }

            if (mediaRepository.findByType(MediaType.SLIDE).isEmpty()) {
                String[] fileNames = {
                        "event-1.jpg",
                        "event-2.jpg",
                        "event-3.jpg",
                        "event-4.jpg",
                        "event-5.jpg",
                        "event-6.png"
                };

                for (int i = 0; i < fileNames.length; i++) {
                    try {
                        ClassPathResource resource = new ClassPathResource("static/events/" + fileNames[i]);
                        byte[] bytes = resource.getInputStream().readAllBytes();

                        mediaService.uploadFromBytes(
                                bytes,
                                i+1,
                                MediaType.SLIDE,
                                "Slide " + (i + 1),
                                "Mô tả slide " + (i + 1),
                                "",
                                "https://web.facebook.com/profile.php?id=61569362034971&locale=vi_VN"
                        );
                    } catch (IOException ignored) {
                    }
                }
            }
        };
    }
}
