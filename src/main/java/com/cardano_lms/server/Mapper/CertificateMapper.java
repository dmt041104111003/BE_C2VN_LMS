package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.CertificateUpdateRequest;
import com.cardano_lms.server.DTO.Response.CertificateResponse;
import com.cardano_lms.server.Entity.Certificate;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CertificateMapper {

    @Mapping(source = "enrollment.id", target = "enrollmentId")
    @Mapping(source = "enrollment.user.id", target = "userId")
    @Mapping(expression = "java(cert.getEnrollment().getUser().getFullName())", target = "name")
    @Mapping(source = "enrollment.course.id", target = "courseId")
    @Mapping(source = "enrollment.course.title", target = "courseTitle")
    @Mapping(source = "enrollment.walletAddress", target = "walletAddress")
    @Mapping(source = "imgUrl", target = "imgUrl")
    @Mapping(source = "qrUrl", target = "qrUrl")
    @Mapping(source = "txHash", target = "txHash")
    @Mapping(source = "policyId", target = "policyId")
    @Mapping(source = "assetName", target = "assetName")
    CertificateResponse toResponse(Certificate cert);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCertificateFromRequest(CertificateUpdateRequest request, @MappingTarget Certificate certificate);
}
