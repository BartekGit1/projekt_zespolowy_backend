package pl.lodz.p.zesp.bid.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlaceBidRequest(
        @NotNull
        Long auctionId,
        @NotNull
        BigDecimal amount
) {
}
