package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.LectureQnaCreateRequest;
import com.cardano_lms.server.DTO.Request.LectureQnaReplyCreateRequest;
import com.cardano_lms.server.DTO.Request.LectureCommentCreateRequest;
import com.cardano_lms.server.DTO.Response.LectureQnaReplyResponse;
import com.cardano_lms.server.DTO.Response.LectureQnaResponse;
import com.cardano_lms.server.Service.LectureQnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Q&A Bài giảng", description = "API hỏi đáp cho bài giảng: tạo câu hỏi, trả lời, thích, ẩn/hiện và xóa bình luận")
@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureQnaController {

        private final LectureQnaService lectureQnaService;

        @Operation(summary = "Lấy danh sách hỏi đáp của bài giảng", description = "Trả về danh sách câu hỏi/bình luận và phản hồi theo bài giảng")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách Q&A thành công")
        })
        @GetMapping("/{lectureId}/qna")
        public ApiResponse<List<LectureQnaResponse>> list(
                        @Parameter(description = "ID bài giảng", required = true) @PathVariable Long lectureId) {
                return ApiResponse.<List<LectureQnaResponse>>builder()
                                .message("Get QnA success")
                                .result(lectureQnaService.getQnaByLecture(lectureId))
                                .build();
        }
        
        @Operation(summary = "Lấy tất cả hỏi đáp của khóa học", description = "Trả về danh sách tất cả câu hỏi/bình luận theo từng bài giảng trong khóa học")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách Q&A thành công")
        })
        @GetMapping("/course/{courseId}/qna")
        public ApiResponse<List<LectureQnaResponse>> listByCourse(
                        @Parameter(description = "ID khóa học", required = true) @PathVariable String courseId) {
                return ApiResponse.<List<LectureQnaResponse>>builder()
                                .message("Get course QnA success")
                                .result(lectureQnaService.getQnaByCourse(courseId))
                                .build();
        }

        @Operation(summary = "Tạo câu hỏi mới cho bài giảng", description = "Người học tạo câu hỏi/bình luận trong phần Q&A của bài giảng")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo câu hỏi thành công")
        })
        @PostMapping("/{lectureId}/qna")
        public ApiResponse<LectureQnaResponse> create(
                        @Parameter(description = "ID bài giảng", required = true) @PathVariable Long lectureId,
                        @RequestBody LectureQnaCreateRequest request) {
                return ApiResponse.<LectureQnaResponse>builder()
                                .message("Create QnA success")
                                .result(lectureQnaService.createQuestion(lectureId, request))
                                .build();
        }

        @Operation(summary = "Trả lời bình luận/câu hỏi", description = "Trả lời vào một bình luận/câu hỏi cụ thể")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trả lời thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
        })
        @PostMapping("/comments/{commentId}/reply")
        public ApiResponse<LectureQnaReplyResponse> reply(
                        @Parameter(description = "ID bình luận", required = true) @PathVariable Long commentId,
                        @RequestBody LectureQnaReplyCreateRequest request) {
                return ApiResponse.<LectureQnaReplyResponse>builder()
                                .message("Reply success")
                                .result(lectureQnaService.reply(commentId, request))
                                .build();
        }

        @Operation(summary = "Cập nhật nội dung bình luận/câu hỏi", description = "Chỉnh sửa nội dung bình luận/câu hỏi")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật bình luận thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
        })
        @PutMapping("/comments/{commentId}")
        public ApiResponse<LectureQnaResponse> update(
                        @Parameter(description = "ID bình luận", required = true) @PathVariable Long commentId,
                        @RequestBody LectureCommentCreateRequest request) {
                return ApiResponse.<LectureQnaResponse>builder()
                                .message("Update comment success")
                                .result(lectureQnaService.update(commentId, request))
                                .build();
        }

        @Operation(summary = "Xóa bình luận/câu hỏi", description = "Xóa một bình luận/câu hỏi trong Q&A")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa bình luận thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
        })
        @DeleteMapping("/comments/{commentId}")
        public ApiResponse<Void> delete(
                        @Parameter(description = "ID bình luận", required = true) @PathVariable Long commentId) {
                lectureQnaService.delete(commentId);
                return ApiResponse.<Void>builder()
                                .message("Delete comment success")
                                .build();
        }

        @Operation(summary = "Thích bình luận/câu hỏi", description = "Tăng số lượt thích cho bình luận/câu hỏi")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thao tác thích thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
        })
        @PostMapping("/comments/{commentId}/like")
        public ApiResponse<LectureQnaResponse> like(
                        @Parameter(description = "ID bình luận", required = true) @PathVariable Long commentId) {
                return ApiResponse.<LectureQnaResponse>builder()
                                .message("Like success")
                                .result(lectureQnaService.like(commentId))
                                .build();
        }

        @Operation(summary = "Không thích bình luận/câu hỏi", description = "Đánh dấu không thích cho bình luận/câu hỏi")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thao tác không thích thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
        })
        @PostMapping("/comments/{commentId}/dislike")
        public ApiResponse<LectureQnaResponse> dislike(
                        @Parameter(description = "ID bình luận", required = true) @PathVariable Long commentId) {
                return ApiResponse.<LectureQnaResponse>builder()
                                .message("Dislike success")
                                .result(lectureQnaService.dislike(commentId))
                                .build();
        }
        
        @Operation(summary = "Bỏ react bình luận/câu hỏi", description = "Xóa react (like/dislike) khỏi bình luận/câu hỏi")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đã bỏ react"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
        })
        @DeleteMapping("/comments/{commentId}/reaction")
        public ApiResponse<LectureQnaResponse> removeReaction(
                        @Parameter(description = "ID bình luận", required = true) @PathVariable Long commentId) {
                return ApiResponse.<LectureQnaResponse>builder()
                                .message("Reaction removed")
                                .result(lectureQnaService.removeReaction(commentId))
                                .build();
        }

        @Operation(summary = "Ẩn bình luận/câu hỏi", description = "Ẩn một bình luận/câu hỏi khỏi danh sách hiển thị")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ẩn bình luận thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
        })
        @PutMapping("/comments/{commentId}/hide")
        public ApiResponse<LectureQnaResponse> hide(
                        @Parameter(description = "ID bình luận", required = true) @PathVariable Long commentId) {
                return ApiResponse.<LectureQnaResponse>builder()
                                .message("Hide comment success")
                                .result(lectureQnaService.hide(commentId))
                                .build();
        }

        @Operation(summary = "Bỏ ẩn bình luận/câu hỏi", description = "Khôi phục hiển thị bình luận/câu hỏi đã bị ẩn")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bỏ ẩn bình luận thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
        })
        @PutMapping("/comments/{commentId}/unhide")
        public ApiResponse<LectureQnaResponse> unhide(
                        @Parameter(description = "ID bình luận", required = true) @PathVariable Long commentId) {
                return ApiResponse.<LectureQnaResponse>builder()
                                .message("Unhide comment success")
                                .result(lectureQnaService.unhide(commentId))
                                .build();
        }
}
