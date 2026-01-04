package com.cardano_lms.server.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_methods")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentMethod {
    @Id
    private String name;

    private String description;
    private String currency;

    @OneToMany(mappedBy = "paymentMethod", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JsonBackReference
    private List<CoursePaymentMethod> coursePaymentMethods = new ArrayList<>();
}
