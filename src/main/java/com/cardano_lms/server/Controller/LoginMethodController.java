package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.LoginMethodRequest;
import com.cardano_lms.server.DTO.Request.RoleRequest;
import com.cardano_lms.server.DTO.Response.LoginMethodResponse;
import com.cardano_lms.server.DTO.Response.RoleResponse;
import com.cardano_lms.server.Service.LoginMethodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Phương thức đăng nhập", description = "API quản lý phương thức đăng nhập")
@RestController
@RequestMapping("/api/login_methods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginMethodController {
    LoginMethodService loginMethodService;

    @Operation(summary = "Tạo phương thức đăng nhập", description = "Thêm mới một phương thức đăng nhập")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping
    ApiResponse<LoginMethodResponse> create(
            @Parameter(description = "Dữ liệu phương thức đăng nhập", required = true) @RequestBody LoginMethodRequest request) {
        return ApiResponse.<LoginMethodResponse>builder()
                .result(loginMethodService.createNewLoginMethod(request))
                .build();
    }

    @Operation(summary = "Xóa phương thức đăng nhập", description = "Xóa theo tên phương thức")
    @DeleteMapping("/{name}")
    ApiResponse<Void> delete(@Parameter(description = "Tên phương thức", required = true) @PathVariable String name) {
        loginMethodService.deleteLoginMethod(name);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Danh sách phương thức đăng nhập", description = "Liệt kê tất cả phương thức đăng nhập")
    @GetMapping
    ApiResponse<List<LoginMethodResponse>> getAll() {
        return ApiResponse.<List<LoginMethodResponse>>builder()
                .result(loginMethodService.getAllLoginMethods())
                .build();
    }
}
