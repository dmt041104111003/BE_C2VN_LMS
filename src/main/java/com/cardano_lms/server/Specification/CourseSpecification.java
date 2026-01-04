package com.cardano_lms.server.Specification;


import com.cardano_lms.server.constant.CourseType;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    public static Specification<Course> titleOrInstructorNameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            Join<Course, User> instructorJoin = root.join("instructor", JoinType.LEFT);

            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), pattern);
            Predicate instructorPredicate = cb.like(cb.lower(instructorJoin.get("fullName")), pattern);

            return cb.or(titlePredicate, instructorPredicate);
        };
    }


    public static Specification<Course> hasCourseType(CourseType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() :
                cb.equal(root.get("courseType"), type);
    }

    public static Specification<Course> instructorNameContains(String instructorName) {
        return (root, query, cb) -> {
            if (instructorName == null || instructorName.trim().isEmpty()) return null;
            Join<Object, Object> instructor = root.join("instructor");
            return cb.like(cb.lower(instructor.get("fullName")), "%" + instructorName.toLowerCase() + "%");
        };
    }

    public static Specification<Course> priceBetween(Integer minPrice, Integer maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            if (minPrice != null && maxPrice != null)
                return cb.between(root.get("price"), minPrice, maxPrice);
            if (minPrice != null)
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Course> hasTagId(String tagId) {
        return (root, query, cb) -> {
            if (tagId == null) return null;
            Join<Object, Object> tags = root.join("courseTags", JoinType.LEFT);
            return cb.equal(tags.get("id"), tagId);
        };
    }

    public static Specification<Course> isDraft(Boolean draft) {
        return (root, query, cb) -> {
            if (draft == null) return null;
            return cb.equal(root.get("draft"), draft);
        };
    }
}
