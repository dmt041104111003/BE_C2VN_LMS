package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.AddStudentRequest;
import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.EnrollCourseRequest;
import com.cardano_lms.server.DTO.Request.ValidatePaymentRequest;
import com.cardano_lms.server.DTO.Response.*;
import com.cardano_lms.server.Entity.Enrollment;
import com.cardano_lms.server.Service.EnrollmentService;
import com.cloudinary.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Đăng ký khóa học", description = "API quản lý đăng ký khóa học, thanh toán và trạng thái hoàn thành")
@RestController
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EnrollmentController {
        EnrollmentService enrollmentService;

        @Operation(summary = "Lấy danh sách khóa học đã đăng ký của tôi", description = "Trả về danh sách các khóa học mà người dùng hiện tại đã đăng ký")
        @GetMapping("/me")
        public ApiResponse<List<MyEnrollmentResponse>> getMyEnrollments() {
                return ApiResponse.<List<MyEnrollmentResponse>>builder()
                        .result(enrollmentService.getMyEnrollments())
                        .message("My enrollments retrieved successfully")
                        .build();
        }

        @Operation(summary = "Xác thực giao dịch thanh toán", description = "Kiểm tra giao dịch có hợp lệ hay không dựa trên sender/receiver/amount/txHash")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kiểm tra thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ")
        })
        @GetMapping("/validate")
        public boolean checkValidPayment(
                        @Parameter(description = "Dữ liệu xác thực thanh toán", required = true) @RequestBody ValidatePaymentRequest request) {
                return enrollmentService.verifyPayment(request.getSender(), request.getReceiver(), request.getAmount(),
                                request.getTxHash());
        }

        @Operation(summary = "Tạo đăng ký sau thanh toán", description = "Tạo bản ghi enrollment cho người dùng sau khi thanh toán thành công")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng ký khóa học thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
        })
        @PostMapping
        public ApiResponse<EnrollmentResponse> enrollACourse(
                        @Parameter(description = "Yêu cầu đăng ký khóa học", required = true) @RequestBody EnrollCourseRequest request) {
                EnrollmentResponse enrollment = enrollmentService.createEnrollmentAfterPayment(request);

                return ApiResponse.<EnrollmentResponse>builder()
                                .message("Enroll this course success")
                                .result(enrollment)
                                .build();
        }

        @Operation(summary = "Lấy lịch sử thanh toán của người dùng", description = "Trả về danh sách giao dịch thanh toán theo người dùng")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn thành công")
        })
        @GetMapping("/payment-history/user/{id}")
        public ApiResponse<List<PaymentHistoryResponse>> getHistory(
                        @Parameter(description = "ID người dùng", required = true) @PathVariable String id) {
                List<PaymentHistoryResponse> paymentHistoryResponses = enrollmentService
                                .getPaymentHistoryByUserId(id);

                return ApiResponse.<List<PaymentHistoryResponse>>builder()
                                .message("Enrolled course")
                                .result(paymentHistoryResponses)
                                .build();
        }

        @Operation(summary = "Đánh dấu hoàn thành khóa học", description = "Đặt trạng thái hoàn thành khóa học cho một người dùng")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi đăng ký")
        })
        @PutMapping("/course/{courseId}/user/{userId}/complete")
        public ApiResponse<String> markCourseCompleted(
                        @Parameter(description = "ID người dùng", required = true) @PathVariable String userId,
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                enrollmentService.updateCourseCompletionStatus(userId, courseId);

                return ApiResponse.<String>builder()
                                .code(200)
                                .message("Course marked as completed successfully")
                                .result("COMPLETED")
                                .build();
        }

        @Operation(summary = "Thống kê đăng ký theo khóa học", description = "Trả về số liệu/chi tiết đăng ký của một khóa học")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn thành công")
        })
        @GetMapping("/course/{courseId}/enrolled")
        public ApiResponse<CourseEnrolledResponse> getEnrolled(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                return ApiResponse.<CourseEnrolledResponse>builder()
                                .code(200)
                                .message("Enrolled query successfully")
                                .result(enrollmentService.getAllEnrolledByCourse(courseId))
                                .build();
        }

        @Operation(summary = "Giảng viên tạo đăng ký thủ công", description = "Tạo enrollment cho người học bởi giảng viên")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo đăng ký thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học hoặc người dùng")
        })
        @PostMapping("/course/{courseId}/user/{userId}/set-enroll")
        public ApiResponse<Enrollment> setEnrollByInstructor(
                        @Parameter(description = "ID người dùng", required = true) @PathVariable String userId,
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                return ApiResponse.<Enrollment>builder()
                                .code(1000)
                                .message("Set Enrolled successfully")
                                .result(enrollmentService.createEnrollment(userId, courseId))
                                .build();
        }

        @Operation(summary = "Thêm học viên vào khóa học", description = "Giảng viên thêm học viên bằng email hoặc địa chỉ ví")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thêm học viên thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
        })
        @PostMapping("/course/{courseId}/add-student")
        public ApiResponse<Enrollment> addStudentToCourse(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "Thông tin liên hệ học viên", required = true) @RequestBody AddStudentRequest request) {
                return ApiResponse.<Enrollment>builder()
                                .code(1000)
                                .message("Thêm học viên thành công")
                                .result(enrollmentService.addStudentByContact(courseId, request.getContactType(), request.getContactValue()))
                                .build();
        }

        @Operation(summary = "Xóa đăng ký", description = "Xóa một bản ghi enrollment theo ID")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi đăng ký")
        })
        @DeleteMapping("/delete/{enrolledId}")
        public ApiResponse<Void> deleteEnrolled(
                        @Parameter(description = "ID đăng ký", required = true) @PathVariable Long enrolledId,
                        @Parameter(description = "ID khóa học", required = true) @RequestParam String courseId) {
                enrollmentService.deleteEnrollment(enrolledId, courseId);
                return ApiResponse.<Void>builder()
                                .code(1000)
                                .message("Delete enrolled successfully")
                                .build();
        }

        @Operation(summary = "Xóa enrollment của chính mình", description = "User tự xóa enrollment của mình (dùng khi face enroll thất bại)")
        @DeleteMapping("/{enrollmentId}")
        public ApiResponse<Void> deleteMyEnrollment(@PathVariable Long enrollmentId) {
                enrollmentService.deleteMyEnrollment(enrollmentId);
                return ApiResponse.<Void>builder()
                                .code(200)
                                .message("Enrollment deleted successfully")
                                .build();
        }

        @GetMapping("/{enrolledId}")
        @Operation(summary = "Lấy chi tiết tiến độ của đăng ký", description = "Trả về tiến độ học tập của một enrollment (dành cho giảng viên)")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đăng ký")
        })
        public ApiResponse<ProgressResponse> getEnrollmentDetail(
                        @Parameter(description = "ID đăng ký", required = true) @PathVariable Long enrolledId) {
                return ApiResponse.<ProgressResponse>builder()
                                .code(1000)
                                .message("Get enrolled successfully")
                                .result(enrollmentService.getEnrollmentProgressByInstructor(enrolledId))
                                .build();
        }

        @GetMapping("/course/{courseId}/upgrade-info")
        @Operation(summary = "Kiểm tra phiên bản khóa học", description = "Kiểm tra xem khóa học có phiên bản mới hơn bản snapshot không")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn thành công")
        })
        public ApiResponse<CourseUpgradeInfoResponse> checkCourseUpgrade(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                return ApiResponse.<CourseUpgradeInfoResponse>builder()
                                .code(200)
                                .message("Upgrade info retrieved successfully")
                                .result(enrollmentService.checkCourseUpgrade(courseId))
                                .build();
        }

        @PostMapping("/course/{courseId}/upgrade")
        @Operation(summary = "Nâng cấp phiên bản khóa học", description = "Nâng cấp lên phiên bản mới nhất của khóa học. CẢNH BÁO: Tiến độ sẽ bị đặt lại!")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Nâng cấp thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy enrollment")
        })
        public ApiResponse<CourseUpgradeInfoResponse> upgradeCourseSnapshot(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                return ApiResponse.<CourseUpgradeInfoResponse>builder()
                                .code(200)
                                .message("Course upgraded successfully")
                                .result(enrollmentService.upgradeCourseSnapshot(courseId))
                                .build();
        }

}
