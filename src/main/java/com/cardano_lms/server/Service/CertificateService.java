package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Request.BatchMintRequest;
import com.cardano_lms.server.DTO.Request.CertificateRequest;
import com.cardano_lms.server.DTO.Response.BatchMintResponse;
import com.cardano_lms.server.DTO.Response.CertificateResponse;
import com.cardano_lms.server.DTO.Response.MintResponse;
import com.cardano_lms.server.Entity.Certificate;
import com.cardano_lms.server.Entity.Enrollment;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.CertificateMapper;
import com.cardano_lms.server.Repository.CertificateRepository;
import com.cardano_lms.server.Repository.EnrollmentRepository;
import com.cardano_lms.server.Repository.UserRepository;
import com.cardano_lms.server.constant.CertificateNFTStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

        private static final String ASSET_PREFIX = "C2VN";
        private static final String DATE_FORMAT = "dd/MM/yyyy";
        private static final int MAX_BATCH_SIZE = 15;
        private static final String DESCRIPTION_TEMPLATE = 
                "This certificate verifies that %s has successfully completed the course '%s' " +
                "instructed by %s. The course was completed on %s and this blockchain-based " +
                "certificate serves as immutable proof of achievement. Certificate ID: %s";

        @Value("${PINATA_URL}")
        private String pinataUrl;

        @Value("${FRONTEND_URL}")
        private String frontendUrl;

        private final CertificateRepository certificateRepository;
        private final EnrollmentRepository enrollmentRepository;
        private final CertificateMapper certificateMapper;
        private final UserRepository userRepository;
        private final PinataService pinataService;
        private final ContractClient contractClient;
        private final CertificateImageGenerator imageGenerator;
        private final BlockchainVerificationService blockchainVerificationService;

        private String generateAssetName(Long enrollmentId) {
                return ASSET_PREFIX + enrollmentId + System.currentTimeMillis();
        }

        private Map<String, String> buildMetadata(String assetName, String studentName, String courseName, String instructorName, String imageUrl, String formattedDate) {
                Map<String, String> metadata = new HashMap<>();
                metadata.put("name", assetName);
                metadata.put("description", String.format(DESCRIPTION_TEMPLATE, studentName, courseName, instructorName, formattedDate, assetName));
                metadata.put("image", imageUrl);
                metadata.put("status", "Completed");
                metadata.put("studentName", studentName);
                metadata.put("courseName", courseName);
                metadata.put("issuedBy", instructorName);
                metadata.put("issuedDate", formattedDate);
                metadata.put("certificateType", "Dynamic NFT with CIP-68 standard");
                metadata.put("platform", "Cardano2VN Learning Management System");
                return metadata;
        }

        private String uploadCertificateImage(Long enrollmentId, String studentName, String courseName, String instructorName) throws Exception {
                byte[] certImage = imageGenerator.generateCertificate(studentName, courseName, instructorName);
                String filename = "cert_" + enrollmentId + "_" + System.currentTimeMillis() + ".png";
                String ipfsHash = pinataService.uploadBytesToIpfs(certImage, filename);
                return pinataUrl + ipfsHash;
        }

        public List<CertificateResponse> getMyCertificates() {
                String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
                return certificateRepository.findByEnrollment_User_Id(currentUserId)
                                .stream()
                                .map(certificateMapper::toResponse)
                                .collect(Collectors.toList());
        }

        public List<CertificateResponse> getCertificatesByUser(String userId) {
                if (!userRepository.existsById(userId)) {
                        throw new AppException(ErrorCode.USER_NOT_EXISTED);
                }
                return certificateRepository.findByEnrollment_User_Id(userId)
                                .stream()
                                .map(certificateMapper::toResponse)
                                .collect(Collectors.toList());
        }

        public CertificateResponse getCertificateDetail(Long id) {
                Certificate cert = certificateRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.CER_NOT_FOUND));
                return certificateMapper.toResponse(cert);
        }

        public CertificateResponse verifyCertificate(String policyId, String assetName) {
                return certificateRepository.findByPolicyIdAndAssetName(policyId, assetName)
                                .map(certificateMapper::toResponse)
                                .orElse(null);
        }

        public CertificateResponse getCertificateByWalletAndCourse(String walletAddress, String courseTitle) {
                Certificate cert = certificateRepository.findByWalletAddressAndCourseTitle(walletAddress, courseTitle)
                                .orElse(null);
                
                if (cert == null) {
                        return null;
                }

                boolean ownsNft = blockchainVerificationService.verifyNftOwnership(
                        walletAddress, 
                        cert.getPolicyId(), 
                        cert.getAssetName()
                );

                if (!ownsNft) {
                        return null;
                }

                return certificateMapper.toResponse(cert);
        }

        public CertificateResponse verifyCertificateOnchain(String policyId, String assetName, String walletAddress) {
                boolean ownsNft = blockchainVerificationService.verifyNftOwnership(walletAddress, policyId, assetName);
                
                if (!ownsNft) {
                        return null;
                }

                return certificateRepository.findByPolicyIdAndAssetName(policyId, assetName)
                                .map(certificateMapper::toResponse)
                                .orElse(null);
        }

        public CertificateResponse mintCertificate(CertificateRequest request) throws Exception {
                Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

                if (!certificateRepository.findByEnrollmentId(request.getEnrollmentId()).isEmpty()) {
                        throw new AppException(ErrorCode.CERTIFICATE_ALREADY_EXISTS);
                }

                String receiver = request.getStudentWalletAddress();
                if (receiver == null || receiver.isBlank()) {
                        throw new AppException(ErrorCode.WALLET_ADDRESS_REQUIRED);
                }

                if (enrollment.getWalletAddress() == null || enrollment.getWalletAddress().isBlank()) {
                        enrollment.setWalletAddress(receiver);
                        enrollmentRepository.save(enrollment);
                }

                String courseName = enrollment.getCourse().getTitle();
                String instructorName = enrollment.getCourse().getInstructor().getFullName();
                String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
                String assetName = generateAssetName(enrollment.getId());

                String ipfsImageUrl = uploadCertificateImage(enrollment.getId(), request.getStudentName(), courseName, instructorName);
                Map<String, String> metadata = buildMetadata(assetName, request.getStudentName(), courseName, instructorName, ipfsImageUrl, formattedDate);

                MintResponse mintResult = contractClient.mint(
                                String.valueOf(enrollment.getCourse().getId()),
                                assetName,
                                metadata,
                                receiver,
                                1);

                String qrUrl = buildVerifyUrl(receiver, courseName);

                Certificate cert = Certificate.builder()
                                .issuedAt(LocalDateTime.now())
                                .txHash(mintResult.getTxHash())
                                .policyId(mintResult.getPolicyId())
                                .qrUrl(qrUrl)
                                .imgUrl(ipfsImageUrl)
                                .assetName(assetName)
                                .enrollment(enrollment)
                                .certificateNFTStatus(CertificateNFTStatus.SUCCESS)
                                .build();

                return certificateMapper.toResponse(certificateRepository.save(cert));
        }

        public CertificateResponse updateCertificate(@NonNull Long id, @NonNull String newHash) {
                Certificate cert = certificateRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.CER_NOT_FOUND));
                cert.setTxHash(newHash);
                Certificate saved = certificateRepository.save(cert);
                return certificateMapper.toResponse(saved);
        }

        public BatchMintResponse batchMintCertificates(String courseId, List<BatchMintRequest.BatchMintItem> items) throws Exception {
                validateBatchRequest(items);

                String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
                
                List<Map<String, Object>> contractItems = new ArrayList<>();
                List<String> assetNames = new ArrayList<>();
                Map<Long, BatchItemData> batchData = new HashMap<>();

                for (BatchMintRequest.BatchMintItem item : items) {
                        Enrollment enrollment = enrollmentRepository.findById(item.getEnrollmentId())
                                        .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
                        
                        if (!certificateRepository.findByEnrollmentId(item.getEnrollmentId()).isEmpty()) {
                                continue;
                        }

                        String courseName = enrollment.getCourse().getTitle();
                        String instructorName = enrollment.getCourse().getInstructor().getFullName();
                        String assetName = generateAssetName(enrollment.getId());
                        String ipfsImageUrl = uploadCertificateImage(enrollment.getId(), item.getStudentName(), courseName, instructorName);

                        assetNames.add(assetName);
                        String qrUrl = buildVerifyUrl(item.getStudentWalletAddress(), courseName);
                        batchData.put(item.getEnrollmentId(), new BatchItemData(enrollment, assetName, ipfsImageUrl, qrUrl));

                        Map<String, String> metadata = buildMetadata(assetName, item.getStudentName(), courseName, instructorName, ipfsImageUrl, formattedDate);

                        Map<String, Object> contractItem = new HashMap<>();
                        contractItem.put("asset_name", assetName);
                        contractItem.put("metadata", metadata);
                        contractItem.put("receiver", item.getStudentWalletAddress());
                        contractItem.put("quantity", "1");
                        contractItems.add(contractItem);
                }

                if (contractItems.isEmpty()) {
                        throw new AppException(ErrorCode.NO_ELIGIBLE_STUDENTS);
                }

                MintResponse mintResult = contractClient.batchMint(courseId, contractItems);

                saveBatchCertificates(items, batchData, mintResult);

                return BatchMintResponse.builder()
                                .success(true)
                                .txHash(mintResult.getTxHash())
                                .policyId(mintResult.getPolicyId())
                                .mintedCount(contractItems.size())
                                .assetNames(assetNames)
                                .build();
        }

        private void validateBatchRequest(List<BatchMintRequest.BatchMintItem> items) {
                if (items == null || items.isEmpty()) {
                        throw new AppException(ErrorCode.INVALID_REQUEST);
                }
                if (items.size() > MAX_BATCH_SIZE) {
                        throw new AppException(ErrorCode.BATCH_LIMIT_EXCEEDED);
                }
        }

        private void saveBatchCertificates(List<BatchMintRequest.BatchMintItem> items, Map<Long, BatchItemData> batchData, MintResponse mintResult) {
                for (BatchMintRequest.BatchMintItem item : items) {
                        BatchItemData data = batchData.get(item.getEnrollmentId());
                        if (data == null) continue;

                        Certificate cert = Certificate.builder()
                                        .issuedAt(LocalDateTime.now())
                                        .txHash(mintResult.getTxHash())
                                        .policyId(mintResult.getPolicyId())
                                        .qrUrl(data.qrUrl)
                                        .imgUrl(data.imageUrl)
                                        .assetName(data.assetName)
                                        .enrollment(data.enrollment)
                                        .certificateNFTStatus(CertificateNFTStatus.SUCCESS)
                                        .build();
                        certificateRepository.save(cert);
                }
        }

        private record BatchItemData(Enrollment enrollment, String assetName, String imageUrl, String qrUrl) {}

        private String buildVerifyUrl(String walletAddress, String courseTitle) {
                String encodedWallet = URLEncoder.encode(walletAddress, StandardCharsets.UTF_8);
                String encodedCourse = URLEncoder.encode(courseTitle, StandardCharsets.UTF_8);
                return frontendUrl + "/verify?wallet=" + encodedWallet + "&course=" + encodedCourse;
        }

        public void burnCertificate(Long id) {
                Certificate cert = certificateRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.CER_NOT_FOUND));

                try {
                        if (cert.getAssetName() != null && !cert.getAssetName().isBlank()) {
                                contractClient.burn(
                                                String.valueOf(cert.getEnrollment().getCourse().getId()),
                                                cert.getAssetName(),
                                                1);
                        }
                        certificateRepository.deleteById(id);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to burn certificate on blockchain: " + e.getMessage(), e);
                }
        }
}
