package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.ContactMessageRequest;
import com.cardano_lms.server.DTO.Response.ContactMessageResponse;
import com.cardano_lms.server.Service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Liên hệ", description = "API gửi và quản lý tin nhắn liên hệ")
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactMessageController {
    private final ContactMessageService contactMessageService;

    @Operation(summary = "Gửi tin nhắn liên hệ", description = "Người dùng gửi tin nhắn cho admin")
    @PostMapping
    public ApiResponse<ContactMessageResponse> submitMessage(@Valid @RequestBody ContactMessageRequest request) {
        return ApiResponse.<ContactMessageResponse>builder()
                .code(1000)
                .message("Gửi tin nhắn thành công")
                .result(contactMessageService.submitMessage(request))
                .build();
    }

    @Operation(summary = "Lấy tất cả tin nhắn", description = "Admin xem tất cả tin nhắn liên hệ")
    @GetMapping
    public ApiResponse<List<ContactMessageResponse>> getAllMessages() {
        return ApiResponse.<List<ContactMessageResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tin nhắn thành công")
                .result(contactMessageService.getAllMessages())
                .build();
    }

    @Operation(summary = "Lấy tin nhắn chưa đọc", description = "Admin xem tin nhắn chưa đọc")
    @GetMapping("/unread")
    public ApiResponse<List<ContactMessageResponse>> getUnreadMessages() {
        return ApiResponse.<List<ContactMessageResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tin nhắn chưa đọc thành công")
                .result(contactMessageService.getUnreadMessages())
                .build();
    }

    @Operation(summary = "Đếm tin nhắn chưa đọc", description = "Admin xem số lượng tin nhắn chưa đọc")
    @GetMapping("/unread/count")
    public ApiResponse<Long> getUnreadCount() {
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Lấy số lượng tin nhắn chưa đọc thành công")
                .result(contactMessageService.getUnreadCount())
                .build();
    }

    @Operation(summary = "Đánh dấu đã đọc", description = "Admin đánh dấu tin nhắn đã đọc")
    @PutMapping("/{id}/read")
    public ApiResponse<ContactMessageResponse> markAsRead(@PathVariable Long id) {
        return ApiResponse.<ContactMessageResponse>builder()
                .code(1000)
                .message("Đánh dấu đã đọc thành công")
                .result(contactMessageService.markAsRead(id))
                .build();
    }

    @Operation(summary = "Xóa tin nhắn", description = "Admin xóa tin nhắn")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long id) {
        contactMessageService.deleteMessage(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa tin nhắn thành công")
                .build();
    }
}
