package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.RoleRequest;
import com.cardano_lms.server.DTO.Response.RoleResponse;
import com.cardano_lms.server.Service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Vai trò (roles)", description = "API quản lý vai trò hệ thống")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @Operation(summary = "Tạo vai trò", description = "Tạo mới một vai trò")
    @PostMapping
    ApiResponse<RoleResponse> create(
            @Parameter(description = "Dữ liệu vai trò", required = true) @RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @Operation(summary = "Lấy tất cả vai trò", description = "Trả về danh sách tất cả vai trò")
    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @Operation(summary = "Xóa vai trò", description = "Xóa một vai trò theo tên")
    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@Parameter(description = "Tên vai trò", required = true) @PathVariable String role) {
        roleService.delete(role);
        return ApiResponse.<Void>builder().build();
    }
}
