package pl.lodz.p.zesp.bid;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.zesp.bid.dto.PlaceBidRequest;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping("/place")
    public ResponseEntity<Void> placeBid(
            @RequestBody @Valid final PlaceBidRequest request,
            final Authentication authentication
    ) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");
        bidService.placeBid(request, username);
        return ResponseEntity.noContent().build();
    }
}
