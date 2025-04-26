package pl.lodz.p.zesp.auction;

import jakarta.validation.constraints.NotEmpty;
import pl.lodz.p.zesp.bid.BidHistogramEntry;

import java.util.List;

public record AuctionHistogramResponse(
        @NotEmpty
        List<BidHistogramEntry> days
) {
}
