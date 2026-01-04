package com.cardano_lms.server.Entity;

import com.cardano_lms.server.constant.OrderStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime enrolledAt;
    private boolean completed;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JsonBackReference
    @JsonIgnore
    @JoinColumn(name = "course_payment_method_id", nullable = false)
    private CoursePaymentMethod coursePaymentMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String orderId;
    private double price;
    
    private String txHash;
    private String senderAddress;
    
    private String walletAddress;

    @JsonIgnore
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    

    @JsonIgnore
    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Progress progress;

    @JsonIgnore
    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL)
    private Certificate certificate;

    @JsonIgnore
    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private EnrolledSnapshot snapshot;
}
