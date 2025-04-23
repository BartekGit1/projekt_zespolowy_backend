package pl.lodz.p.zesp.bid;

import org.springframework.stereotype.Component;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.bid.dto.PlaceBidRequest;
import pl.lodz.p.zesp.user.UserEntity;

import java.time.LocalDateTime;

@Component
public class BidMapper {

    public BidEntity convert(final PlaceBidRequest request, final AuctionEntity auction, final Long userId) {
        final var bid = new BidEntity();
        bid.setAmount(request.amount());
        bid.setBidTime(LocalDateTime.now());
        bid.setAuction(auction);

        final var user = new UserEntity();
        user.setId(userId);
        bid.setUser(user);

        return bid;
    }
}
