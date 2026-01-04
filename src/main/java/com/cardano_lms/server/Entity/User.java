package com.cardano_lms.server.Entity;

import com.cardano_lms.server.constant.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "email", unique = true, nullable = true)
    String email;
    String password;

    @Column(unique = true, nullable = true)
    String google;

    @Column(unique = true, nullable = true)
    String github;
    
    String expertise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    String fullName;

    @Column(columnDefinition = "TEXT")
    String bio;

    @Column(unique = true, nullable = true)
    String walletAddress;

    @ManyToOne
    @JoinColumn(name = "role_name")
    Role role;

    @ManyToOne
    @JoinColumn(name = "login_method_name")
    LoginMethod loginMethod;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Course> courses = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void normalize() {
        if (email != null) email = email.trim().toLowerCase();
        if (google != null) google = google.trim().toLowerCase();
        if (github != null) github = github.trim().toLowerCase();
        if (walletAddress != null) walletAddress = walletAddress.trim();
        if (fullName != null) fullName = fullName.trim();
    }

}
