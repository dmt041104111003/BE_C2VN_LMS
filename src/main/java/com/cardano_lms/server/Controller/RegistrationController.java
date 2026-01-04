package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.*;
import com.cardano_lms.server.Service.UserService;
import com.cardano_lms.server.Service.VerificationService;
import com.cardano_lms.server.constant.PredefineLoginMethod;
import com.cardano_lms.server.Repository.UserRepository;
import com.cardano_lms.server.constant.UserStatus;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Đăng ký/Xác thực email", description = "API đăng ký tài khoản và xác thực email")
public class RegistrationController {
    UserService userService;
    VerificationService verificationService;
    UserRepository userRepository;

    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới và gửi mã xác thực qua email")
    @PostMapping("/register")
    public ApiResponse<String> register(
            @Parameter(description = "Dữ liệu đăng ký", required = true) @RequestBody RegisterRequest request) {
        var existingOpt = userRepository.findByEmailIgnoreCase(request.getEmail());
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (existing.getStatus() == UserStatus.INACTIVE 
                && existing.getLoginMethod() != null 
                && PredefineLoginMethod.EMAIL_PASSWORD_METHOD.equals(existing.getLoginMethod().getName())) {
                verificationService.resend(existing.getEmail());
                return ApiResponse.<String>builder()
                        .message("Email đã tồn tại nhưng chưa xác thực. Đã gửi lại mã xác thực.")
                        .result("OK")
                        .build();
            }
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        UserCreationRequest createReq = UserCreationRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .loginMethod(PredefineLoginMethod.EMAIL_PASSWORD_METHOD)
                .build();
        userService.createUser(createReq);
        verificationService.startVerification(request.getEmail());
        return ApiResponse.<String>builder().message("Đã gửi mã xác thực").result("OK").build();
    }

    @Operation(summary = "Xác thực email", description = "Xác nhận mã xác thực được gửi tới email")
    @PostMapping("/verify-email")
    public ApiResponse<String> verifyEmail(
            @Parameter(description = "Dữ liệu xác thực email", required = true) @RequestBody VerifyEmailRequest request) {
        verificationService.verifyCode(request.getEmail(), request.getCode());
        return ApiResponse.<String>builder().message("Xác thực thành công").result("OK").build();
    }

    @Operation(summary = "Gửi lại mã xác thực", description = "Gửi lại mã xác thực tới email người dùng")
    @PostMapping("/resend-code")
    public ApiResponse<String> resend(
            @Parameter(description = "Email cần gửi lại mã", required = true) @RequestBody ResendCodeRequest request) {
        verificationService.resend(request.getEmail());
        return ApiResponse.<String>builder().message("Đã gửi lại mã xác thực").result("OK").build();
    }
}
