package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Response.CourseDetailResponse;
import com.cardano_lms.server.Service.LearnService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Học tập", description = "API cho trang học tập: lấy nội dung khóa học đã đăng ký của người dùng")
@RestController
@RequestMapping("/api/learn")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearnController {

    LearnService learnService;

    @Operation(summary = "Lấy nội dung học tập của khóa học", description = "Trả về dữ liệu cho trang học tập (chỉ hiển thị nội dung cho người đã đăng ký)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Chưa đăng ký khóa học hoặc không có quyền truy cập")
    })
    @GetMapping("/{courseId}")
    public ApiResponse<CourseDetailResponse> getCourseForLearning(
            @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
        return ApiResponse.<CourseDetailResponse>builder()
                .result(learnService.getCourseForLearning(courseId))
                .build();
    }
}
