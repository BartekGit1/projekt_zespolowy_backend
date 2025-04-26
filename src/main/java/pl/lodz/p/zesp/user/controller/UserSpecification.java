package pl.lodz.p.zesp.user.controller;

import org.springframework.data.jpa.domain.Specification;
import pl.lodz.p.zesp.user.UserEntity;

public class UserSpecification {

    public static Specification<UserEntity> hasGeneralFilter(String filter) {
        return (root, query, criteriaBuilder) -> {
            final var lowerCaseFilter = "%" + filter.toLowerCase() + "%";
            final var usernameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), lowerCaseFilter);
            final var emailLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), lowerCaseFilter);
            final var roleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), lowerCaseFilter);
            return criteriaBuilder.or(usernameLike, emailLike, roleLike);
        };
    }
}
