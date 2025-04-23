package pl.lodz.p.zesp.bid;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.auction.AuctionService;
import pl.lodz.p.zesp.bid.dto.PlaceBidRequest;
import pl.lodz.p.zesp.common.util.api.exception.BadRequestException;
import pl.lodz.p.zesp.common.util.api.exception.ConflictException;
import pl.lodz.p.zesp.user.UserService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Log4j2
public class BidService {

    private final BidRepository bidRepository;
    private final BidMapper bidMapper;
    private final AuctionService auctionService;
    private final UserService userService;

    public void placeBid(final PlaceBidRequest request, final String username) {
        final var auction = auctionService.getById(request.auctionId());
        validatePlaceBid(request.amount(), auction);
        final var userData = userService.getUserData(username);
        final var bid = bidMapper.convert(request, auction, userData.id());
        bidRepository.save(bid);
        log.info("Bid has been placed for auction {}, user {}, amount {}", auction.getId(), username, request.amount());
    }

    private void validatePlaceBid(final BigDecimal amount, final AuctionEntity auction) {
        if (auction.isFinished()) {
            throw new ConflictException("Auction has ended. No possible to place new bids.");
        }
        final var optionalBid = auction.getBids().stream().findFirst();
        if (optionalBid.isPresent() && optionalBid.get().getAmount().compareTo(amount) >= 0) {
            throw new ConflictException("Auction has bid with bigger or equal amount.");
        }
        if (auction.getStartingPrice().compareTo(amount) > 0) {
            throw new BadRequestException("Amount has to be greater or equal to minimum amount.");
        }
    }
}
