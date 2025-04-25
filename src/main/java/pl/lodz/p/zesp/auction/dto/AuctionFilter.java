package pl.lodz.p.zesp.auction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import pl.lodz.p.zesp.auction.AuctionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
public class AuctionFilter {
    AuctionStatus status;
    String title;
    String description;
    Boolean promoted;
    Long userId;
    LocalDateTime endDateBefore;
    LocalDateTime endDateAfter;
    BigDecimal minPrice;
    BigDecimal maxPrice;
    String filter;

    public Specification<AuctionEntity> buildSpecification() {
        Specification<AuctionEntity> spec = Specification.where(null);

        if (Objects.nonNull(this.getStatus())) {
            spec = spec.and(AuctionSpecification.hasStatus(this.getStatus()));
        }
        if (StringUtils.hasLength(this.getTitle())) {
            spec = spec.and(AuctionSpecification.hasTitleLike(this.getTitle()));
        }
        if (StringUtils.hasLength(this.getDescription())) {
            spec = spec.and(AuctionSpecification.hasDescriptionLike(this.getDescription()));
        }
        if (Objects.nonNull(this.getPromoted())) {
            spec = spec.and(AuctionSpecification.isPromoted());
        }
        if (Objects.nonNull(this.getUserId())) {
            spec = spec.and(AuctionSpecification.hasUser(this.getUserId()));
        }
        if (Objects.nonNull(this.getEndDateBefore())) {
            spec = spec.and(AuctionSpecification.hasEndDateBefore(this.getEndDateBefore()));
        }
        if (Objects.nonNull(this.getEndDateAfter())) {
            spec = spec.and(AuctionSpecification.hasEndDateAfter(this.getEndDateAfter()));
        }
        if (Objects.nonNull(this.getMinPrice())) {
            spec = spec.and(AuctionSpecification.hasStartingPriceGreaterThanOrEqualTo(this.getMinPrice()));
        }
        if (Objects.nonNull(this.getMaxPrice())) {
            spec = spec.and(AuctionSpecification.hasStartingPriceLessThanOrEqualTo(this.getMaxPrice()));
        }
        if (StringUtils.hasLength(this.getFilter())) {
            spec = spec.and(AuctionSpecification.hasGeneralFilter(this.getFilter()));
        }
        return spec;
    }
}
