package pl.lodz.p.zesp.auction.dto;

import org.springframework.data.jpa.domain.Specification;
import pl.lodz.p.zesp.auction.AuctionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionSpecification {

    public static Specification<AuctionEntity> hasStatus(final AuctionStatus auctionStatus) {
        return (root, query, criteriaBuilder) -> {
            switch (auctionStatus) {
                case ACTIVE -> {
                    return criteriaBuilder.equal(root.get("finished"), false);
                }
                case FINISHED -> {
                    return criteriaBuilder.equal(root.get("finished"), true);
                }
                default -> {
                    return criteriaBuilder.conjunction();
                }
            }
        };
    }

    public static Specification<AuctionEntity> isPromoted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isPromoted"), true);
    }

    public static Specification<AuctionEntity> hasTitleLike(final String title) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<AuctionEntity> hasDescriptionLike(final String description) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<AuctionEntity> hasUser(final Long userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<AuctionEntity> hasEndDateBefore(final LocalDateTime dateTime) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("endDate"), dateTime);
    }

    public static Specification<AuctionEntity> hasEndDateAfter(final LocalDateTime dateTime) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("endDate"), dateTime);
    }

    public static Specification<AuctionEntity> hasStartingPriceGreaterThanOrEqualTo(final BigDecimal price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("startingPrice"), price);
    }

    public static Specification<AuctionEntity> hasStartingPriceLessThanOrEqualTo(final BigDecimal price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("startingPrice"), price);
    }
}
