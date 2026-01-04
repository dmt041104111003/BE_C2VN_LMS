package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.UserCreationRequest;
import com.cardano_lms.server.DTO.Request.UpdateNameRequest;
import com.cardano_lms.server.DTO.Request.UpdateProfileRequest;
import com.cardano_lms.server.DTO.Request.ChangePasswordRequest;
import com.cardano_lms.server.DTO.Request.UserUpdateRoleRequest;
import com.cardano_lms.server.DTO.Response.PageResponse;
import com.cardano_lms.server.DTO.Response.UserResponse;
import com.cardano_lms.server.Service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Người dùng", description = "API quản lý người dùng: tạo, tìm kiếm, cập nhật, khóa/mở khóa và thông tin cá nhân")
public class UserController {
    UserService userService;

    @Operation(summary = "Tạo người dùng", description = "Tạo mới người dùng theo dữ liệu đầu vào")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo người dùng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping
    ApiResponse<UserResponse> createUser(
            @Parameter(description = "Dữ liệu tạo người dùng", required = true) @RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .message("User created successfully")
                .build();
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Operation(summary = "Tìm kiếm người dùng", description = "Tìm kiếm và phân trang theo từ khóa, vai trò, trạng thái")
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "Vai trò") @RequestParam(required = false) String role,
            @Parameter(description = "Trạng thái") @RequestParam(required = false) String status,
            @Parameter(description = "Trang hiện tại (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> userPage = userService.searchUsers(keyword, role, status, PageRequest.of(page, size));

        PageResponse<UserResponse> response = PageResponse.<UserResponse>builder()
                .content(userPage.getContent())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();

        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(response)
                .build();
    }

    @Operation(summary = "Khóa tài khoản", description = "Khóa tài khoản người dùng theo ID")
    @PutMapping("/{userId}/ban")
    public ApiResponse<String> banUser(
            @Parameter(description = "ID người dùng", required = true) @PathVariable String userId) {
        userService.banUser(userId);
        return ApiResponse.<String>builder()
                .message("User has been banned successfully")
                .result("BANNED")
                .build();
    }

    @Operation(summary = "Mở khóa tài khoản", description = "Mở khóa tài khoản người dùng theo ID")
    @PutMapping("/{userId}/unban")
    public ApiResponse<String> unbanUser(
            @Parameter(description = "ID người dùng", required = true) @PathVariable String userId) {
        userService.unbanUser(userId);
        return ApiResponse.<String>builder()
                .message("User has been unbanned successfully")
                .result("ACTIVE")
                .build();
    }

    @Operation(summary = "Lấy chi tiết người dùng", description = "Trả về thông tin chi tiết của người dùng theo ID")
    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(
            @Parameter(description = "ID người dùng", required = true) @PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @Operation(summary = "Tìm người dùng theo email", description = "Trả về thông tin người dùng với email cung cấp")
    @GetMapping("/by-email")
    public ApiResponse<UserResponse> getUserByEmail(
            @Parameter(description = "Email người dùng", required = true) @RequestParam String email) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserByEmail(email))
                .build();
    }

    @Operation(summary = "Lấy thông tin của tôi", description = "Trả về thông tin người dùng hiện tại")
    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @Operation(summary = "Xóa người dùng", description = "Xóa người dùng theo ID")
    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(
            @Parameter(description = "ID người dùng", required = true) @PathVariable String userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @Operation(summary = "Cập nhật vai trò người dùng", description = "Cập nhật role cho người dùng theo ID")
    @PutMapping("/updateRole/{userId}")
    ApiResponse<UserResponse> updateUser(
            @Parameter(description = "ID người dùng", required = true) @PathVariable String userId,
            @Parameter(description = "Dữ liệu cập nhật vai trò", required = true) @RequestBody UserUpdateRoleRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserRole(userId, request))
                .build();
    }

    @Operation(summary = "Cập nhật tên của tôi", description = "Người dùng tự cập nhật tên hiển thị")
    @PutMapping("/me/name")
    ApiResponse<UserResponse> updateMyName(
            @Parameter(description = "Dữ liệu cập nhật tên", required = true) @RequestBody UpdateNameRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyName(request))
                .message("Updated name successfully")
                .build();
    }

    @Operation(summary = "Cập nhật hồ sơ của tôi", description = "Người dùng tự cập nhật thông tin hồ sơ")
    @PutMapping("/me/profile")
    ApiResponse<UserResponse> updateMyProfile(
            @Parameter(description = "Dữ liệu cập nhật hồ sơ", required = true) @RequestBody UpdateProfileRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyProfile(request))
                .message("Updated profile successfully")
                .build();
    }

    @Operation(summary = "Đổi mật khẩu của tôi", description = "Người dùng tự đổi mật khẩu")
    @PutMapping("/me/password")
    ApiResponse<String> changeMyPassword(
            @Parameter(description = "Dữ liệu đổi mật khẩu", required = true) @RequestBody ChangePasswordRequest request) {
        userService.changeMyPassword(request);
        return ApiResponse.<String>builder()
                .result("Password updated successfully")
                .build();
    }

}
