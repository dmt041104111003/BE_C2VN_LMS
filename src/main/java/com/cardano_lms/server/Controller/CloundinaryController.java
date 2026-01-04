package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.Entity.Media;
import com.cardano_lms.server.constant.MediaType;
import com.cardano_lms.server.Service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.List;

@Tag(name = "Media", description = "API quản lý media (Cloudinary): upload, cập nhật, xóa, và liệt kê")
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class CloundinaryController {

    private final MediaService mediaService;

    @Operation(summary = "Upload media", description = "Tải lên tệp media tới Cloudinary")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Upload thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Upload thất bại")
    })
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Media>> uploadMedia(
            @Parameter(description = "Tệp media", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "Loại media") @RequestParam(defaultValue = "EVENTIMG") MediaType type,
            @Parameter(description = "Tiêu đề") @RequestParam(required = false) String title,
            @Parameter(description = "Mô tả") @RequestParam(required = false) String description,
            @Parameter(description = "Vị trí") @RequestParam(required = false) String location) {
        try {
            Media media = mediaService.upload(file, type, title, description, location);
            return ResponseEntity.ok(
                    ApiResponse.<Media>builder()
                            .message("Upload media successfully")
                            .result(media)
                            .build());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Media>builder()
                            .code(400)
                            .message("Upload failed: " + e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "Cập nhật media", description = "Cập nhật thông tin media, có thể thay thế file")
    @PutMapping
    public ResponseEntity<ApiResponse<Media>> updateMedia(
            @Parameter(description = "Tệp media mới") @RequestParam(required = false) MultipartFile file,
            @Parameter(description = "Loại media") @RequestParam(defaultValue = "EVENTIMG") MediaType type,
            @Parameter(description = "ID media", required = true) @RequestParam Long id,
            @Parameter(description = "Tiêu đề") @RequestParam(required = false) String title,
            @Parameter(description = "Mô tả") @RequestParam(required = false) String description,
            @Parameter(description = "Vị trí") @RequestParam(required = false) String location,
            @Parameter(description = "Liên kết") @RequestParam(required = false) String link) {
        try {
            Media updated = mediaService.editMedia(id, file, type, title, description, location, link);
            return ResponseEntity.ok(
                    ApiResponse.<Media>builder()
                            .message("Media updated successfully")
                            .result(updated)
                            .build());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Media>builder()
                            .code(400)
                            .message("Update failed: " + e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Media>builder()
                            .code(404)
                            .message(e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "Xóa media", description = "Xóa media theo publicId trên Cloudinary")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @Parameter(description = "Public ID của media", required = true) @RequestParam("publicId") String publicId) {
        try {
            boolean success = mediaService.delete(publicId);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .message(success ? "Delete success" : "Failed to delete")
                            .build());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<Void>builder()
                            .code(500)
                            .message("Error: " + e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "Danh sách media", description = "Liệt kê media theo loại")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Media>>> getAll(
            @Parameter(description = "Loại media") @RequestParam(defaultValue = "EVENTIMG") MediaType type) {
        List<Media> list = mediaService.getAll(type);
        return ResponseEntity.ok(
                ApiResponse.<List<Media>>builder()
                        .message("Fetched successfully")
                        .result(list)
                        .build());
    }

    @Operation(summary = "Upload video", description = "Tải lên video tới Cloudinary (dành cho Instructor)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Upload thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Upload thất bại")
    })
    @PostMapping("/upload-video")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> uploadVideo(
            @Parameter(description = "Tệp video", required = true) @RequestParam("file") MultipartFile file) {
        try {
            java.util.Map<String, Object> result = mediaService.uploadVideo(file);
            return ResponseEntity.ok(
                    ApiResponse.<java.util.Map<String, Object>>builder()
                            .message("Upload video successfully")
                            .result(result)
                            .build());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<java.util.Map<String, Object>>builder()
                            .code(400)
                            .message("Upload failed: " + e.getMessage())
                            .build());
        }
    }
}
