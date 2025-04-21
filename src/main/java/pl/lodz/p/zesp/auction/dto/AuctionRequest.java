package pl.lodz.p.zesp.auction.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionRequest(
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotNull
        @Positive
        BigDecimal startingPrice,
        @NotNull
        @Future
        LocalDateTime endDate,
        @NotNull
        Long userId,
        @NotNull
        Boolean isPromoted,
        @NotBlank
        String uri
) {
}
