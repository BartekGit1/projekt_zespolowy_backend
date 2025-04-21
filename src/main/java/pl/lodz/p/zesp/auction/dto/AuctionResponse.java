package pl.lodz.p.zesp.auction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.bid.BidEntity;
import pl.lodz.p.zesp.user.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuctionResponse(
        @NotNull
        Long id,
        @NotNull
        Long version,
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotNull
        @Positive
        BigDecimal startingPrice,
        List<BidResponse> bids,
        @NotNull
        LocalDateTime endDate,
        @NotNull
        UserResponse user,
        @NotNull
        Boolean isPromoted,
        @NotNull
        LocalDateTime createdAt,
        @NotNull
        Boolean finished,
        @NotNull
        Boolean paid,
        @NotBlank
        String uri
) {

    public static AuctionResponse of(final AuctionEntity auction) {
        return new AuctionResponse(
                auction.getId(),
                auction.getVersion(),
                auction.getTitle(),
                auction.getDescription(),
                auction.getStartingPrice(),
                auction.getBids().stream().map(BidResponse::of).toList(),
                auction.getEndDate(),
                UserResponse.of(auction.getUser()),
                auction.isPromoted(),
                auction.getCreatedAt(),
                auction.isFinished(),
                auction.isPaid(),
                auction.getUri()
        );
    }

    record BidResponse(
            Long id,
            BigDecimal amount,
            LocalDateTime bidTime
    ) {
        public static BidResponse of(final BidEntity bid) {
            return new BidResponse(
                    bid.getId(),
                    bid.getAmount(),
                    bid.getBidTime()
            );
        }
    }

    record UserResponse(
        Long id,
        String username
    ) {
        public static UserResponse of(final UserEntity user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername()
            );
        }
    }
}
