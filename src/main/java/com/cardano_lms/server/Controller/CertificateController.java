
package com.cardano_lms.server.Controller;

import com.cardano_lms.server.DTO.Request.ApiResponse;
import com.cardano_lms.server.DTO.Request.BatchMintRequest;
import com.cardano_lms.server.DTO.Request.CertificateRequest;
import com.cardano_lms.server.DTO.Request.CertificateUpdateRequest;
import com.cardano_lms.server.DTO.Response.BatchMintResponse;
import com.cardano_lms.server.DTO.Response.CertificateResponse;
import com.cardano_lms.server.Service.CertificateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chứng chỉ", description = "API quản lý chứng chỉ: xem, mint NFT, cập nhật và thu hồi")
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

        private final CertificateService certificateService;

        @Operation(summary = "Lấy chứng chỉ của người dùng hiện tại", description = "Trả về tất cả chứng chỉ thuộc về người dùng đã đăng nhập")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách chứng chỉ thành công")
        })
        @GetMapping("/me")
        public ApiResponse<List<CertificateResponse>> getMyCertificates() {
                return ApiResponse.<List<CertificateResponse>>builder()
                                .message("Lấy danh sách chứng chỉ thành công")
                                .result(certificateService.getMyCertificates())
                                .build();
        }

        @Operation(summary = "Lấy chứng chỉ theo người dùng", description = "Trả về tất cả chứng chỉ thuộc về một người dùng")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách chứng chỉ thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
        })
        @GetMapping("/user/{userId}")
        public ApiResponse<List<CertificateResponse>> getCertificatesByUser(
                        @Parameter(description = "ID người dùng", required = true) @PathVariable String userId) {
                return ApiResponse.<List<CertificateResponse>>builder()
                                .message("Get certificates of user success")
                                .result(certificateService.getCertificatesByUser(userId))
                                .build();
        }

        @Operation(summary = "Lấy chi tiết chứng chỉ", description = "Trả về thông tin chi tiết của một chứng chỉ cụ thể")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy chi tiết chứng chỉ thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy chứng chỉ")
        })
        @GetMapping("/{id}")
        public ApiResponse<CertificateResponse> getCertificateDetail(
                        @Parameter(description = "ID chứng chỉ", required = true) @PathVariable Long id) {
                return ApiResponse.<CertificateResponse>builder()
                                .message("Get certificate detail success")
                                .result(certificateService.getCertificateDetail(id))
                                .build();
        }

        @Operation(summary = "Xác minh chứng chỉ", description = "Xác minh tính hợp lệ của chứng chỉ bằng Policy ID và Asset Name")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xác minh thành công")
        })
        @GetMapping("/verify")
        public ApiResponse<CertificateResponse> verifyCertificate(
                        @Parameter(description = "Policy ID", required = true) @RequestParam String policyId,
                        @Parameter(description = "Asset Name", required = true) @RequestParam String assetName) {
                return ApiResponse.<CertificateResponse>builder()
                                .message("Xác minh chứng chỉ")
                                .result(certificateService.verifyCertificate(policyId, assetName))
                                .build();
        }

        @Operation(summary = "Xác minh chứng chỉ theo địa chỉ ví và khóa học", description = "Xác minh chứng chỉ của một địa chỉ ví cho một khóa học cụ thể")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xác minh thành công")
        })
        @GetMapping("/verify/wallet")
        public ApiResponse<CertificateResponse> verifyCertificateByWalletAndCourse(
                        @Parameter(description = "Địa chỉ ví Cardano", required = true) @RequestParam String walletAddress,
                        @Parameter(description = "Tên khóa học", required = true) @RequestParam String courseTitle) {
                return ApiResponse.<CertificateResponse>builder()
                                .message("Xác minh chứng chỉ")
                                .result(certificateService.getCertificateByWalletAndCourse(walletAddress, courseTitle))
                                .build();
        }

        @Operation(summary = "Xác minh chứng chỉ on-chain", description = "Xác minh NFT certificate trực tiếp trên blockchain")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xác minh thành công")
        })
        @GetMapping("/verify/onchain")
        public ApiResponse<CertificateResponse> verifyCertificateOnchain(
                        @Parameter(description = "Policy ID", required = true) @RequestParam String policyId,
                        @Parameter(description = "Asset Name", required = true) @RequestParam String assetName,
                        @Parameter(description = "Địa chỉ ví Cardano", required = true) @RequestParam String walletAddress) {
                return ApiResponse.<CertificateResponse>builder()
                                .message("Xác minh chứng chỉ on-chain")
                                .result(certificateService.verifyCertificateOnchain(policyId, assetName, walletAddress))
                                .build();
        }

        @Operation(summary = "Cấp chứng chỉ", description = "Tạo và cấp chứng chỉ NFT lên blockchain (tự sinh ảnh)")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cấp chứng chỉ thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu yêu cầu không hợp lệ"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        @PostMapping("/mint")
        public ApiResponse<CertificateResponse> mintCertificate(@RequestBody CertificateRequest request)
                        throws Exception {
                CertificateResponse res = certificateService.mintCertificate(request);
                return ApiResponse.<CertificateResponse>builder()
                                .message("Cấp chứng chỉ thành công")
                                .result(res)
                                .build();
        }

        @Operation(summary = "Cấp chứng chỉ hàng loạt", description = "Cấp chứng chỉ NFT cho nhiều học viên (tự sinh ảnh riêng cho từng người)")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cấp chứng chỉ hàng loạt thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc vượt quá giới hạn"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        @PostMapping("/batch-mint")
        public ApiResponse<BatchMintResponse> batchMintCertificates(@RequestBody BatchMintRequest request)
                        throws Exception {
                BatchMintResponse res = certificateService.batchMintCertificates(request.getCourseId(), request.getItems());
                return ApiResponse.<BatchMintResponse>builder()
                                .message("Cấp chứng chỉ hàng loạt thành công")
                                .result(res)
                                .build();
        }

        @Operation(summary = "Cập nhật chứng chỉ", description = "Cập nhật transaction hash/IPFS hash cho chứng chỉ")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật chứng chỉ thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy chứng chỉ")
        })
        @PutMapping("/{id}")
        public ApiResponse<CertificateResponse> updateCertificate(
                        @Parameter(description = "ID chứng chỉ", required = true) @NonNull @PathVariable Long id,
                        @Parameter(description = "Dữ liệu cập nhật chứng chỉ", required = true) @NonNull @RequestBody CertificateUpdateRequest request) {
                return ApiResponse.<CertificateResponse>builder()
                                .message("Update certificate success")
                                .result(certificateService.updateCertificate(id, request.getIpfsHash()))
                                .build();
        }

        @Operation(summary = "Thu hồi (burn) chứng chỉ", description = "Burn NFT chứng chỉ trên blockchain và xóa khỏi cơ sở dữ liệu")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thu hồi chứng chỉ thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy chứng chỉ"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Thu hồi chứng chỉ thất bại")
        })
        @DeleteMapping("/{id}")
        public ApiResponse<Void> burnCertificate(
                        @Parameter(description = "ID chứng chỉ", required = true) @PathVariable Long id) {
                certificateService.burnCertificate(id);
                return ApiResponse.<Void>builder()
                                .message("Burn certificate success")
                                .build();
        }

}
