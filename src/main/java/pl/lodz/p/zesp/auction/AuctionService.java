package pl.lodz.p.zesp.auction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.lodz.p.zesp.auction.dto.AuctionFilter;
import pl.lodz.p.zesp.auction.dto.AuctionRequest;
import pl.lodz.p.zesp.auction.dto.AuctionResponse;
import pl.lodz.p.zesp.common.util.IdResponse;
import pl.lodz.p.zesp.common.util.api.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;

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
        final var updatedAuction = auctionMapper.convert(auction, request);
        auctionRepository.save(updatedAuction);
    }

    void deleteAuction(final Long auctionId) {
        auctionRepository.deleteById(auctionId);
    }

    private AuctionEntity getById(final Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NotFoundException("Auction not found"));
    }
}
