package pl.lodz.p.zesp.auction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.zesp.auction.dto.AuctionFilter;
import pl.lodz.p.zesp.auction.dto.AuctionRequest;
import pl.lodz.p.zesp.auction.dto.AuctionResponse;
import pl.lodz.p.zesp.common.util.IdResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
class AuctionController {

    private final AuctionService auctionService;

    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable final Long auctionId) {
        return ResponseEntity.ok(auctionService.getAuctionById(auctionId));
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAllAuctions(final AuctionFilter auctionFilter) {
        return ResponseEntity.ok(auctionService.getAuctions(auctionFilter));
    }

    @PutMapping
    public ResponseEntity<IdResponse> addAuction(
            @RequestBody @Valid final AuctionRequest request
    ) {
        final var response = auctionService.addAuction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{auctionId}")
    public ResponseEntity<Void> updateAuction(
            @PathVariable final Long auctionId,
            @RequestBody @Valid final AuctionRequest request
    ) {
        auctionService.updateAuction(auctionId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> deleteAuction(@PathVariable final Long auctionId) {
        auctionService.deleteAuction(auctionId);
        return ResponseEntity.noContent().build();
    }
}
