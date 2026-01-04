package com.cardano_lms.server.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_payment_methods")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CoursePaymentMethod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "course_id")
    @JsonManagedReference
    private Course course;

    @ManyToOne @JoinColumn(name = "payment_method_id")
    @JsonManagedReference
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String receiverAddress;
}
