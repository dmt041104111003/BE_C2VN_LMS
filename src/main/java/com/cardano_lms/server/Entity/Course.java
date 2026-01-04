package com.cardano_lms.server.Entity;

import com.cardano_lms.server.constant.CourseType;
import com.cardano_lms.server.constant.Currency;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;
    
    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String shortDescription;
    @Column(columnDefinition = "TEXT")
    private String requirement;
    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    @Column(columnDefinition = "TEXT")
    private String videoUrl;
    private boolean draft;
    private Integer price;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private CourseType courseType;

    private Double discount;
    private LocalDateTime discountEndTime;
    private String policyId;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonManagedReference
    @ToString.Exclude
    private User instructor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String publicIdImage;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Chapter> chapters = new ArrayList<>();

    @Builder.Default
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "course_tags", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @JsonManagedReference
    @ToString.Exclude
    private List<Tag> courseTags = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Test> courseTests = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Enrollment> enrollments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<CoursePaymentMethod> coursePaymentMethods = new ArrayList<>();

    public void addTest(Test test) {
        courseTests.add(test);
        test.setCourse(this);
    }

    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
        chapter.setCourse(this);
    }
}
