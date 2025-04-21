package pl.lodz.p.zesp.auction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.lodz.p.zesp.auction.dto.AuctionFilter;
import pl.lodz.p.zesp.auction.dto.AuctionRequest;
import pl.lodz.p.zesp.auction.dto.AuctionResponse;
import pl.lodz.p.zesp.common.util.IdResponse;
import pl.lodz.p.zesp.common.util.api.exception.NotFoundException;

import java.util.List;

import static org.springframework.data.domain.Sort.Order.asc;

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

    List<AuctionResponse> getAuctions(final AuctionFilter filter) {
        final var defaultSort = Sort.by(asc("finished"), asc("endDate"));
        return auctionRepository.findAll(filter.buildSpecification(), defaultSort).stream()
                .map(AuctionResponse::of)
                .toList();
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
