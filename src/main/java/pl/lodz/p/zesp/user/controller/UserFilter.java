package pl.lodz.p.zesp.user.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import pl.lodz.p.zesp.user.UserEntity;

@Getter
@Setter
@Builder
public class UserFilter {
    String filter;

    public Specification<UserEntity> buildSpecification() {
        Specification<UserEntity> spec = Specification.where(null);

        if (StringUtils.hasLength(this.getFilter())) {
            spec = spec.and(UserSpecification.hasGeneralFilter(this.getFilter()));
        }
        return spec;
    }
}
