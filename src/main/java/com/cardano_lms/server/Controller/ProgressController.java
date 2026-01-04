package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.EnrollCourseRequest;
import com.cardano_lms.server.DTO.Request.ProgressCreationRequest;
import com.cardano_lms.server.DTO.Request.ValidatePaymentRequest;
import com.cardano_lms.server.DTO.Response.ActivityResponse;
import com.cardano_lms.server.DTO.Response.PaymentHistoryResponse;
import com.cardano_lms.server.DTO.Response.PaymentMethodResponse;
import com.cardano_lms.server.DTO.Response.ProgressResponse;
import com.cardano_lms.server.Entity.Enrollment;
import com.cardano_lms.server.Entity.Progress;
import com.cardano_lms.server.Service.EnrollmentService;
import com.cardano_lms.server.Service.ProgressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Tiến độ học tập", description = "API theo dõi tiến độ và hoạt động học tập của người dùng")
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressController {
    ProgressService progressService;

    @Operation(summary = "Lấy tiến độ của người dùng", description = "Trả về tiến độ học tập theo khóa học của người dùng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy tiến độ thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.name")
    public ApiResponse<List<ProgressResponse>> getUserProgress(
            @Parameter(description = "ID người dùng", required = true) @PathVariable String userId) {
        List<ProgressResponse> response = progressService.getUserProgress(userId);
        return ApiResponse.<List<ProgressResponse>>builder()
                .message("Course progress")
                .result(response)
                .build();
    }

    @Operation(summary = "Tạo/cập nhật tiến độ", description = "Khởi tạo hoặc cập nhật tiến độ học tập của người dùng trong khóa học")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo/cập nhật tiến độ thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @PostMapping("/user/{userId}/course/{courseId}")
    @PreAuthorize("#userId == authentication.name")
    public ApiResponse<Progress> createProgress(
            @Parameter(description = "Yêu cầu tạo tiến độ", required = true) @RequestBody ProgressCreationRequest request,
            @Parameter(description = "ID người dùng", required = true) @PathVariable String userId,
            @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
        Progress progress = progressService.createProgress(request, userId, courseId);
        return ApiResponse.<Progress>builder()
                .message("Course progress")
                .result(progress)
                .build();
    }

    @Operation(summary = "Lấy hoạt động học tập", description = "Trả về các hoạt động học tập gần đây của người dùng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy hoạt động thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping("/user/{userId}/activity")
    @PreAuthorize("#userId == authentication.name")
    public ApiResponse<List<ActivityResponse>> getUserActivity(
            @Parameter(description = "ID người dùng", required = true) @PathVariable String userId) {
        return progressService.getUserActivity(userId);
    }

}
