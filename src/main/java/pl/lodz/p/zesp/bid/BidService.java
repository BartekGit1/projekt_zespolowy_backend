package pl.lodz.p.zesp.bid;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.auction.AuctionService;
import pl.lodz.p.zesp.bid.dto.PlaceBidRequest;
import pl.lodz.p.zesp.common.util.api.exception.BadRequestException;
import pl.lodz.p.zesp.common.util.api.exception.ConflictException;
import pl.lodz.p.zesp.user.Role;
import pl.lodz.p.zesp.user.UserEntity;
import pl.lodz.p.zesp.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static pl.lodz.p.zesp.common.util.Constants.BID_LIMIT;

@Service
@RequiredArgsConstructor
@Log4j2
public class BidService {

    private final BidRepository bidRepository;
    private final BidMapper bidMapper;
    private final AuctionService auctionService;
    private final UserService userService;
    private final AutoBidRepository autoBidRepository;

    public void placeBid(final PlaceBidRequest request, final String username) {
        final var auction = auctionService.getById(request.auctionId());
        validatePlaceBid(request.amount(), auction, username);

        final var user = userService.getUserEntity(username);

        if (request.autoBid()) {
            handleAutoBid(request, auction, user);
            final var currentHighest = getCurrentHighestBid(auction);
            final var tempBid = new BidEntity(auction, user, currentHighest.getAmount(), LocalDateTime.now());
            triggerAutoBids(auction, tempBid);
            return;
        }

        final var bid = bidMapper.convert(request, auction, user.getId());
        bid.setAmount(request.amount());

        final var currentHighestBid = getCurrentHighestBid(auction);
        if (user.equals(currentHighestBid.getUser()) &&
                currentHighestBid.getAmount().compareTo(bid.getAmount()) >= 0) {
            return;
        }

        bidRepository.save(bid);
        log.info("Bid has been placed for auction {}, user {}, amount {}", auction.getId(), username, bid.getAmount());

        triggerAutoBids(auction, bid);

        autoBidRepository.findByAuctionAndUser(auction, currentHighestBid.getUser())
                .filter(AutoBidEntity::isActive)
                .ifPresent(autoBid -> {
                    if (autoBid.getMaxAmount().compareTo(bid.getAmount()) < 0) {
                        deactivateAutoBid(autoBid, auction);
                    }
                });
    }

    private void handleAutoBid(PlaceBidRequest request, AuctionEntity auction, UserEntity user) {
        if (!Role.PREMIUM.equals(user.getRole())) {
            throw new BadRequestException("only premium users can perform auto bid");
        }

        final var existingAutoBid = autoBidRepository.findByAuctionAndUser(auction, user);
        final var maxAmount = request.amount();
        final var now = LocalDateTime.now();
        final var autoBid = existingAutoBid.orElseGet(() ->
                new AutoBidEntity(user, auction, maxAmount, now));

        if (maxAmount.compareTo(autoBid.getMaxAmount()) > 0) {
            autoBid.setMaxAmount(maxAmount);
            autoBid.setCreatedAt(now);
        }

        if (!autoBid.isActive()) {
            autoBid.setActive(true);
        }

        autoBidRepository.save(autoBid);
    }

    private void validatePlaceBid(final BigDecimal amount, final AuctionEntity auction, final String username) {
        if (amount.compareTo(BID_LIMIT) > 0) {
            throw new BadRequestException("That big bid value is not supported");
        }

        if (auction.isFinished()) {
            throw new ConflictException("Auction has ended. No possible to place new bids.");
        }

        final var currentHighest = getCurrentHighestBid(auction);

        if (currentHighest.getUser() != null &&
                currentHighest.getUser().getUsername().equals(username)) {
            throw new ConflictException("You are the highest bidder");
        }

        if (currentHighest.getAmount().compareTo(amount) >= 0) {
            throw new ConflictException("Auction has bid with bigger or equal amount.");
        }

        if (auction.getStartingPrice().compareTo(amount) > 0) {
            throw new BadRequestException("Amount has to be greater or equal to minimum amount.");
        }
    }

    private BidEntity getCurrentHighestBid(AuctionEntity auction) {
        return auction.getBids().stream()
                .max(Comparator.comparing(BidEntity::getAmount))
                .orElseGet(() ->
                        new BidEntity(auction, null, auction.getStartingPrice(), LocalDateTime.now()));
    }

    private void triggerAutoBids(AuctionEntity auction, BidEntity currentBid) {
        final List<AutoBidEntity> biddersList = autoBidRepository.findAllByAuctionAndActiveTrue(auction).stream()
                .filter(autoBid -> autoBid.getMaxAmount().compareTo(currentBid.getAmount().add(BigDecimal.ONE)) >= 0)
                .sorted(Comparator.comparing(AutoBidEntity::getMaxAmount).reversed()
                        .thenComparing(AutoBidEntity::getCreatedAt))
                .toList();

        if (biddersList.isEmpty()) return;

        final var topBidder = biddersList.get(0);

        if (biddersList.size() == 1) {
            final var bidAmount = currentBid.getAmount().add(BigDecimal.ONE);
            if (bidAmount.compareTo(topBidder.getMaxAmount()) <= 0) {
                placeAutoBid(auction, topBidder, bidAmount);
            } else {
                deactivateAutoBid(topBidder, auction);
            }
            return;
        }

        final var secondBidder = biddersList.get(1);
        final var finalBidAmount = calculateAutoBidAmount(topBidder, secondBidder);

        if (finalBidAmount.compareTo(currentBid.getAmount()) > 0) {
            placeAutoBid(auction, topBidder, finalBidAmount);
        }

        for (int i = 1; i < biddersList.size(); i++) {
            final var bidder = biddersList.get(i);
            if (bidder.getMaxAmount().compareTo(finalBidAmount) <= 0) {
                deactivateAutoBid(bidder, auction);

            }
        }
    }

    private BigDecimal calculateAutoBidAmount(AutoBidEntity topBidder, AutoBidEntity secondBidder) {
        return secondBidder.getMaxAmount().add(BigDecimal.ONE).min(topBidder.getMaxAmount());
    }

    private void placeAutoBid(AuctionEntity auction, AutoBidEntity autoBid, BigDecimal amount) {
        final var bid = new BidEntity(auction, autoBid.getUser(), amount, LocalDateTime.now());

        bidRepository.save(bid);

        log.info("Auto-bid placed by user {} for auction {}, amount {}",
                autoBid.getUser().getUsername(), auction.getId(), amount);
    }

    private void deactivateAutoBid(AutoBidEntity autoBid, AuctionEntity auction) {
        autoBid.setActive(false);
        autoBidRepository.save(autoBid);

        log.info("Auto-bid for user {} has been deactivated (maxAmount reached) in auction {}",
                autoBid.getUser().getUsername(), auction.getId());
    }
}