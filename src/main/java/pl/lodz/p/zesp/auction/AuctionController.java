package pl.lodz.p.zesp.auction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<Page<AuctionResponse>> getAllAuctions(
            final AuctionFilter auctionFilter,
            final Pageable pageable
    ) {
        return ResponseEntity.ok(auctionService.getAuctions(auctionFilter, pageable));
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

    @GetMapping("/histogram")
    public ResponseEntity<AuctionHistogramResponse> getHistogram(@RequestParam(value = "days", defaultValue = "7") final Long days) {
        final var response = auctionService.getHistogram(days);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/toggle")
    public ResponseEntity<Void>  toggleTrackingAuction(@RequestParam Long auctionId, Authentication authentication) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");
        auctionService.toggleTrackingAuction(auctionId, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/watched")
    public ResponseEntity<List<AuctionResponse>> getAllAuctionsWatchedBySingleUser(
            Authentication authentication
    ) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");
        return ResponseEntity.ok(auctionService.findAllWatchedAuctionsSingleUser(username));
    }
}
