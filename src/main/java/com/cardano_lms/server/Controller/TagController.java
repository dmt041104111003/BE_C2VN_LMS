package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.TagRequest;
import com.cardano_lms.server.DTO.Response.TagResponse;
import com.cardano_lms.server.Service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "Thẻ (tags)", description = "API quản lý thẻ: lấy danh sách, tạo mới, xóa")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Lấy tất cả thẻ", description = "Trả về danh sách tất cả các thẻ")
    @GetMapping
    public ApiResponse<List<TagResponse>> getAllTags() {
        return ApiResponse.<List<TagResponse>>builder()
                .result(tagService.getAllTags())
                .code(1000)
                .message("success")
                .build();

    }

    @Operation(summary = "Tạo thẻ mới", description = "Tạo một thẻ mới")
    @PostMapping
    public ApiResponse<TagResponse> createTag(
            @Parameter(description = "Dữ liệu tạo thẻ", required = true) @RequestBody TagRequest tagRequest) {
        return ApiResponse.<TagResponse>builder()
                .message("success")
                .result(tagService.createTag(tagRequest))
                .code(1000)
                .build();
    }

    @Operation(summary = "Xóa thẻ", description = "Xóa một thẻ theo ID")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTag(@Parameter(description = "ID thẻ", required = true) @PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.<Void>builder().code(1000).message("Tag delete successfullly").build();
    }
}
