package pl.lodz.p.zesp.auction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.lodz.p.zesp.auction.dto.AuctionFilter;
import pl.lodz.p.zesp.auction.dto.AuctionRequest;
import pl.lodz.p.zesp.auction.dto.AuctionResponse;
import pl.lodz.p.zesp.bid.BidEntity;
import pl.lodz.p.zesp.bid.BidRepository;
import pl.lodz.p.zesp.common.util.IdResponse;
import pl.lodz.p.zesp.common.util.api.exception.ConflictException;
import pl.lodz.p.zesp.common.util.api.exception.NotFoundException;
import pl.lodz.p.zesp.payment.PaymentRepository;
import pl.lodz.p.zesp.user.UserEntity;
import pl.lodz.p.zesp.user.UserRepository;
import pl.lodz.p.zesp.watchlist.WatchlistEntity;
import pl.lodz.p.zesp.watchlist.WatchlistRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final BidRepository bidRepository;
    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    AuctionResponse getAuctionById(final Long id) {
        return auctionRepository.findById(id)
                .map(AuctionResponse::of)
                .orElseThrow(() -> new NotFoundException("Auction not found"));
    }

    Page<AuctionResponse> getAuctions(final AuctionFilter filter, final Pageable pageable) {
        return auctionRepository.findAll(filter.buildSpecification(), pageable)
                .map(AuctionResponse::of);
    }

    IdResponse addAuction(final AuctionRequest request) {
        final var auction = auctionMapper.convert(request);
        final var auctionId = auctionRepository.save(auction).getId();
        return new IdResponse(auctionId);
    }

    void updateAuction(final Long auctionId, final AuctionRequest request) {
        final var auction = getById(auctionId);
        validateUpdateAuction(auction, request);
        final var updatedAuction = auctionMapper.convert(auction, request);
        auctionRepository.save(updatedAuction);
    }

    private void validateUpdateAuction(final AuctionEntity auction, final AuctionRequest request) {
        if (auction.isFinished()) {
            throw new ConflictException("You cannot update finished auction");
        }
        final var isBidHigherThanNewStartingPrice = auction.getBids().stream()
                .anyMatch(bid -> request.startingPrice().compareTo(bid.getAmount()) >= 0);
        if (isBidHigherThanNewStartingPrice) {
            throw new ConflictException("There is already bid higher or equal to new starting price");
        }
    }

    void deleteAuction(final Long auctionId) {
        AuctionEntity auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NotFoundException("Auction not found"));
        List<WatchlistEntity> watchlistEntity = watchlistRepository.findByAuction_Id(auctionId);

        if(auction.getBids() != null) {
            for (BidEntity bid : auction.getBids()) {
                bidRepository.delete(bid);
            }
        }

        if (auction.getPayment() != null) {
            paymentRepository.delete(auction.getPayment());
        }

        if(!Objects.isNull(watchlistEntity)){
            watchlistEntity.forEach(watchlistRepository::delete);
        }

        auctionRepository.delete(auction);
    }

    public AuctionEntity getById(final Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NotFoundException("Auction not found"));
    }

    public AuctionHistogramResponse getHistogram(final Long days) {
        final var fromDate = LocalDateTime.now().minusDays(days);
        final var entries = bidRepository.findHistogramFromDate(fromDate);
        return new AuctionHistogramResponse(entries);
    }

    void toggleTrackingAuction(final Long auctionId, final String username) {
        Optional<WatchlistEntity> existingItem = watchlistRepository.findByAuction_IdAndUser_username(auctionId, username);

        if (existingItem.isPresent()) {
            watchlistRepository.delete(existingItem.get());
        } else {
            AuctionEntity auction = auctionRepository.findById(auctionId)
                    .orElseThrow(() -> new NotFoundException("Auction not found"));

            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            WatchlistEntity watchlistEntity = new WatchlistEntity(user, auction);
            watchlistRepository.save(watchlistEntity);
        }
    }

    List<AuctionResponse> findAllWatchedAuctionsSingleUser(final String username) {
        return watchlistRepository.findAllWatchedAuctionsByUser(username).stream()
                .map(watchlistEntity -> AuctionResponse.of(watchlistEntity.getAuction())).toList();
    }

    Page<AuctionResponse> findAllMyAuctions(final String username, final Pageable pageable) {
        return auctionRepository.findAllMyAuctions(username, pageable);
    }

    Page<AuctionResponse> findAllMyWonAuctions(final String username, final Pageable pageable) {
        return auctionRepository.findAllMyWonAuctions(username, pageable);
    }

    Page<AuctionResponse> findAllFinishedAuctions(final Pageable pageable) {
        return auctionRepository.findAllFinishedAuctions(pageable);
    }
}
