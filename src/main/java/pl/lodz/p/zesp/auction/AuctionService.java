package pl.lodz.p.zesp.auction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.lodz.p.zesp.auction.dto.AuctionFilter;
import pl.lodz.p.zesp.auction.dto.AuctionRequest;
import pl.lodz.p.zesp.auction.dto.AuctionResponse;
import pl.lodz.p.zesp.bid.BidRepository;
import pl.lodz.p.zesp.common.util.IdResponse;
import pl.lodz.p.zesp.common.util.api.exception.ConflictException;
import pl.lodz.p.zesp.common.util.api.exception.NotFoundException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final BidRepository bidRepository;

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
        auctionRepository.deleteById(auctionId);
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
}
