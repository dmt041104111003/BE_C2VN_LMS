package com.cardano_lms.server.Specification;

import com.cardano_lms.server.Entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasRole(String role) {
        return (root, query, cb) ->
                role == null ? null : cb.equal(root.get("role").get("name"), role);
    }

    public static Specification<User> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<User> searchKeyword(String keyword) {
      return (root, query, cb) -> {
          if (keyword == null || keyword.isEmpty()) return null;
          String likePattern = "%" + keyword.toLowerCase() + "%";
          return cb.or(
                    cb.like(cb.lower(root.get("email")), likePattern),
                  cb.like(cb.lower(root.get("google")), likePattern),
                    cb.like(cb.lower(root.get("github")), likePattern),
                    cb.like(cb.lower(root.get("walletAddress")), likePattern)
          );
      };
  }
}
