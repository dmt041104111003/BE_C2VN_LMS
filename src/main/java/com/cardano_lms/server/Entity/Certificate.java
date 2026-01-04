package com.cardano_lms.server.Entity;

import com.cardano_lms.server.Entity.Enrollment;
import com.cardano_lms.server.constant.CertificateNFTStatus;
import com.cardano_lms.server.constant.MediaType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "tx_hash", unique = true, length = 255)
    private String txHash;

    @Column(name = "img_url", columnDefinition = "TEXT")
    private String imgUrl;

    @Column(name = "qr_url", columnDefinition = "TEXT")
    private String qrUrl;

    @Column(name = "policy_id")
    private String policyId;

    @Column(name = "asset_name")
    private String assetName;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CertificateNFTStatus certificateNFTStatus;

}
