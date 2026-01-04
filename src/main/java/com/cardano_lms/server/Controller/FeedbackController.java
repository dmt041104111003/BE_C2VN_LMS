package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.FeedbackRequest;
import com.cardano_lms.server.DTO.Response.FeedbackResponse;
import com.cardano_lms.server.DTO.Response.InboxItemResponse;
import com.cardano_lms.server.DTO.Response.PageResponse;
import com.cardano_lms.server.Service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Đánh giá khóa học", description = "API quản lý đánh giá: lấy danh sách, phân trang, thêm, ẩn/hiện và xóa đánh giá")
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

        private final FeedbackService feedbackService;

        @Operation(summary = "Lấy danh sách đánh giá theo khóa học", description = "Trả về danh sách đánh giá của một khóa học. Tham số isAdmin=true sẽ bao gồm cả đánh giá bị ẩn.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn đánh giá thành công")
        })
        @GetMapping("/course/{courseId}")
        public ApiResponse<List<FeedbackResponse>> getFeedbacks(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "Chế độ quản trị: bao gồm cả đánh giá bị ẩn") @RequestParam(defaultValue = "false") boolean isAdmin) {
                return ApiResponse.<List<FeedbackResponse>>builder()
                                .result(feedbackService.getFeedbacksByCourse(courseId, isAdmin))
                                .build();
        }

        @Operation(summary = "Lấy đánh giá theo trang", description = "Trả về danh sách đánh giá theo phân trang cho một khóa học")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn đánh giá theo trang thành công")
        })
        @GetMapping("/course/{courseId}/paged")
        public ApiResponse<PageResponse<FeedbackResponse>> getFeedbacksPaged(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @Parameter(description = "Trang hiện tại (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
                return ApiResponse.<PageResponse<FeedbackResponse>>builder()
                                .result(feedbackService.getFeedbacksByCoursePaged(courseId, page, size))
                                .message("Feedback query successfully")
                                .build();
        }

        @Operation(summary = "Thêm đánh giá cho khóa học", description = "Người học thêm đánh giá/bình luận cho khóa học")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thêm đánh giá thành công")
        })
        @PostMapping("/course/{courseId}")
        public ApiResponse<FeedbackResponse> addFeedback(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId,
                        @RequestBody FeedbackRequest request) {
                return ApiResponse.<FeedbackResponse>builder()
                                .result(feedbackService.addFeedback(courseId, request))
                                .message("Thank for your feedback")
                                .build();
        }

        @Operation(summary = "Cập nhật đánh giá", description = "Chỉnh sửa nội dung và/hoặc rating của đánh giá")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật đánh giá thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền chỉnh sửa")
        })
        @PutMapping("/{feedbackId}")
        public ApiResponse<FeedbackResponse> updateFeedback(
                        @Parameter(description = "ID đánh giá", required = true) @PathVariable Long feedbackId,
                        @RequestBody FeedbackRequest request) {
                return ApiResponse.<FeedbackResponse>builder()
                                .result(feedbackService.updateFeedback(feedbackId, request))
                                .message("Cập nhật đánh giá thành công")
                                .build();
        }

        @Operation(summary = "Xóa đánh giá", description = "Xóa một đánh giá theo ID")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa đánh giá thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá")
        })
        @DeleteMapping("/{feedbackId}")
        public ApiResponse<String> deleteFeedback(
                        @Parameter(description = "ID đánh giá", required = true) @PathVariable Long feedbackId) {
                feedbackService.deleteFeedback(feedbackId);
                return ApiResponse.<String>builder()
                                .result("Feedback deleted successfully")
                                .build();
        }

        @Operation(summary = "Ẩn đánh giá", description = "Ẩn một đánh giá khỏi danh sách hiển thị")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ẩn đánh giá thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá")
        })
        @PutMapping("/{feedbackId}/hide")
        public ApiResponse<String> hideFeedback(
                        @Parameter(description = "ID đánh giá", required = true) @PathVariable Long feedbackId) {
                feedbackService.hideFeedback(feedbackId);
                return ApiResponse.<String>builder()
                                .result("Feedback hidden successfully")
                                .build();
        }

        @Operation(summary = "Bỏ ẩn đánh giá", description = "Khôi phục hiển thị một đánh giá đã bị ẩn")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bỏ ẩn đánh giá thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá")
        })
        @PutMapping("/{feedbackId}/unhide")
        public ApiResponse<String> unHideFeedback(
                        @Parameter(description = "ID đánh giá", required = true) @PathVariable Long feedbackId) {
                feedbackService.unHideFeedback(feedbackId);
                return ApiResponse.<String>builder()
                                .result("Feedback un hidden successfully")
                                .build();
        }

        @Operation(summary = "React to feedback", description = "Like or dislike a feedback. Toggle if already reacted.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reaction saved successfully")
        })
        @PostMapping("/{feedbackId}/react")
        public ApiResponse<FeedbackResponse> reactToFeedback(
                        @Parameter(description = "ID đánh giá", required = true) @PathVariable Long feedbackId,
                        @Parameter(description = "Loại reaction: LIKE hoặc DISLIKE") @RequestParam String type) {
                return ApiResponse.<FeedbackResponse>builder()
                                .result(feedbackService.reactToFeedback(feedbackId, type))
                                .build();
        }

        @Operation(summary = "Lấy đánh giá của tôi", description = "Trả về danh sách tất cả đánh giá và phản hồi của người dùng hiện tại")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn đánh giá thành công")
        })
        @GetMapping("/me")
        public ApiResponse<List<FeedbackResponse>> getMyFeedbacks() {
                return ApiResponse.<List<FeedbackResponse>>builder()
                                .result(feedbackService.getMyFeedbacks())
                                .build();
        }

        @Operation(summary = "Lấy inbox của tôi", description = "Trả về tất cả đánh giá khóa học và hỏi đáp bài giảng của người dùng hiện tại")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Truy vấn inbox thành công")
        })
        @GetMapping("/inbox")
        public ApiResponse<List<InboxItemResponse>> getMyInbox() {
                return ApiResponse.<List<InboxItemResponse>>builder()
                                .result(feedbackService.getMyInbox())
                                .build();
        }
        
        @Operation(summary = "Đếm số inbox chưa đọc", description = "Đếm số lượng đánh giá và hỏi đáp từ người khác trong các khóa học bạn sở hữu")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đếm thành công")
        })
        @GetMapping("/inbox/count")
        public ApiResponse<Long> countUnreadInbox() {
                return ApiResponse.<Long>builder()
                                .result(feedbackService.countUnreadInboxItems())
                                .build();
        }
        
        @Operation(summary = "Đánh dấu đã đọc", description = "Đánh dấu một item trong inbox là đã đọc")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đánh dấu thành công")
        })
        @PostMapping("/inbox/read")
        public ApiResponse<Void> markAsRead(
                @Parameter(description = "Loại item: FEEDBACK hoặc LECTURE_COMMENT", required = true) @RequestParam String itemType,
                @Parameter(description = "ID của item", required = true) @RequestParam Long itemId) {
                feedbackService.markAsRead(itemType, itemId);
                return ApiResponse.<Void>builder()
                                .message("Đã đánh dấu đã đọc")
                                .build();
        }
}
