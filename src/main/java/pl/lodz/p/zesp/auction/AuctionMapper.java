package pl.lodz.p.zesp.auction;

import org.springframework.stereotype.Component;
import pl.lodz.p.zesp.auction.dto.AuctionRequest;
import pl.lodz.p.zesp.user.UserEntity;

import java.util.Optional;

@Component
public class AuctionMapper {

    AuctionEntity convert(final AuctionRequest request) {
        final var auction = new AuctionEntity();
        auction.setTitle(request.title());
        auction.setDescription(request.description());
        auction.setStartingPrice(request.startingPrice());
        auction.setEndDate(request.endDate());
        auction.setPromoted(request.isPromoted());

        final var user = new UserEntity();
        user.setId(request.userId());
        auction.setUser(user);

        return auction;
    }

    public AuctionEntity convert(final AuctionEntity auction, final AuctionRequest request) {
        Optional.ofNullable(request.title()).ifPresent(auction::setTitle);
        Optional.ofNullable(request.description()).ifPresent(auction::setDescription);
        Optional.ofNullable(request.startingPrice()).ifPresent(auction::setStartingPrice);
        Optional.ofNullable(request.endDate()).ifPresent(auction::setEndDate);
        Optional.ofNullable(request.isPromoted()).ifPresent(auction::setPromoted);
        return auction;
    }
}
