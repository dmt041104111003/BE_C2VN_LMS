package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.*;
import com.cardano_lms.server.DTO.Response.AuthenticationResponse;
import com.cardano_lms.server.DTO.Response.IntrospectResponse;
import com.cardano_lms.server.DTO.Response.LogoutResponse;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Service.AuthenticationService;
import com.cardano_lms.server.Service.PasswordResetService;
import com.cardano_lms.server.Service.RateLimitService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.text.ParseException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Xác thực", description = "API xác thực: đăng nhập, introspect, refresh token, logout, quên/đặt lại mật khẩu")
public class AuthenticationController {
    AuthenticationService authenticationService;
    PasswordResetService passwordResetService;
    RateLimitService rateLimitService;

    @Operation(summary = "Đăng nhập lấy token", description = "Xác thực thông tin đăng nhập và trả về JWT; đồng thời set cookie access_token an toàn")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Sai thông tin đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Quá nhiều yêu cầu")
    })
    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(
            @Parameter(description = "Thông tin đăng nhập", required = true) @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        String rateLimitKey = getRateLimitKey(httpRequest, request.getEmail());
        
        if (rateLimitService.isBlocked(rateLimitKey)) {
            long secondsRemaining = rateLimitService.getBlockedSecondsRemaining(rateLimitKey);
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS, 
                "Quá nhiều lần thử. Vui lòng đợi " + (secondsRemaining / 60 + 1) + " phút");
        }

        try {
            var result = authenticationService.authenticate(request);
            
            rateLimitService.recordSuccessfulAttempt(rateLimitKey);

            ResponseCookie cookie = ResponseCookie.from("access_token", result.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(36000)
                    .sameSite("None")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return ApiResponse.<AuthenticationResponse>builder()
                    .result(result)
                    .message("Success")
                    .build();
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.UNAUTHENTICATED || 
                e.getErrorCode() == ErrorCode.USER_NOT_EXISTED) {
                rateLimitService.recordFailedAttempt(rateLimitKey);
                
                int remaining = rateLimitService.getRemainingAttempts(rateLimitKey);
                if (remaining > 0 && remaining <= 3) {
                    throw new AppException(e.getErrorCode(), 
                        e.getMessage() + ". Còn " + remaining + " lần thử");
                }
            }
            throw e;
        }
    }
    
    private String getRateLimitKey(HttpServletRequest request, String email) {
        String ip = getClientIp(request);
        String normalizedEmail = email != null ? email.toLowerCase().trim() : "";
        return ip + ":" + normalizedEmail;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    @Operation(summary = "Yêu cầu quên mật khẩu", description = "Gửi mã đặt lại mật khẩu tới email người dùng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gửi mã thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Quá nhiều yêu cầu")
    })
    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(
            @Parameter(description = "Email cần đặt lại mật khẩu", required = true) @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        String rateLimitKey = "forgot:" + getClientIp(httpRequest);
        
        if (rateLimitService.isBlocked(rateLimitKey)) {
            long secondsRemaining = rateLimitService.getBlockedSecondsRemaining(rateLimitKey);
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS, 
                "Quá nhiều yêu cầu. Vui lòng đợi " + (secondsRemaining / 60 + 1) + " phút");
        }
        
        rateLimitService.recordFailedAttempt(rateLimitKey);
        passwordResetService.startReset(request.getEmail());
        return ApiResponse.<String>builder().result("Reset code sent").build();
    }

    @Operation(summary = "Đặt lại mật khẩu", description = "Xác thực mã và đặt lại mật khẩu mới")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mã không hợp lệ hoặc hết hạn")
    })
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(
            @Parameter(description = "Dữ liệu đặt lại mật khẩu", required = true) @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ApiResponse.<String>builder().result("Password reset successfully").build();
    }

    @Operation(summary = "Introspect token", description = "Kiểm tra hiệu lực của token")
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(
            @Parameter(description = "Yêu cầu introspect", required = true) @RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @Operation(summary = "Làm mới token", description = "Lấy token mới từ refresh token")
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(
            @Parameter(description = "Yêu cầu refresh token", required = true) @RequestBody RefreshRequest request,
            HttpServletResponse response)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        ResponseCookie cookie = ResponseCookie.from("access_token", result.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(36000)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @Operation(summary = "Đăng xuất", description = "Xóa token và hủy cookie")
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(
            @Parameter(description = "Cookie access_token") @CookieValue(name = "access_token", required = false) String token,
            HttpServletResponse response) throws ParseException, JOSEException {

        var result = authenticationService.logout(token);

        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ApiResponse.<LogoutResponse>builder().result(result).build();
    }

}
