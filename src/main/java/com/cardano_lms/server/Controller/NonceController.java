package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.NonceCreationRequest;
import com.cardano_lms.server.DTO.Request.RoleRequest;
import com.cardano_lms.server.DTO.Response.NonceCreationResponse;
import com.cardano_lms.server.DTO.Response.RoleResponse;
import com.cardano_lms.server.Entity.Nonce;
import com.cardano_lms.server.Mapper.NonceMapper;
import com.cardano_lms.server.Repository.NonceRepository;
import com.cardano_lms.server.Service.NonceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Nonce/Web3", description = "API tạo và xác thực nonce cho đăng nhập ví")
@RestController
@RequestMapping("/api/nonce")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NonceController {
    NonceService nonceService;
    NonceMapper nonceMapper;

    @Operation(summary = "Tạo nonce", description = "Sinh nonce mới theo địa chỉ ví để ký xác thực")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo nonce thành công")
    })
    @PostMapping
    public ApiResponse<NonceCreationResponse> generateNonce(
            @Parameter(description = "Dữ liệu tạo nonce (địa chỉ ví)", required = true) @RequestBody NonceCreationRequest nonceCreationRequest) {
        NonceCreationResponse nonce = nonceService
                .generateNonce(nonceMapper.toNonce(nonceCreationRequest).getAddress());
        return ApiResponse.<NonceCreationResponse>builder()
                .result(nonce)
                .build();
    }

    @Operation(summary = "Xác thực nonce", description = "Xác minh chữ ký nonce để đăng nhập")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xác thực thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping("/validate")
    public ApiResponse<Boolean> validateNonce(
            @Parameter(description = "nonce, signature, key", required = true) @RequestBody Map<String, String> body) {
        String nonce = body.get("nonce");
        String signature = body.get("signature");
        String key = body.get("key");
        Boolean valid = nonceService.validateNonce(nonce, signature, key);
        return ApiResponse.<Boolean>builder()
                .result(valid)
                .build();
    }

}
