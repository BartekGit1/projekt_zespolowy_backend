package pl.lodz.p.zesp.payment.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.zesp.payment.PaymentService;

import static pl.lodz.p.zesp.common.util.Constants.URI_PAYMENTS;
import static pl.lodz.p.zesp.common.util.Constants.URI_VERSION_V1;

@RestController
@RequestMapping(URI_VERSION_V1 + URI_PAYMENTS)
class PaymentController {
    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','PREMIUM')")
    @PostMapping
    String createPayment(Authentication authentication) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");

        return paymentService.initPayment(username);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','PREMIUM')")
    @PostMapping("/{auctionId}")
    String createAuctionPayment(@PathVariable Long auctionId, Authentication authentication) {
        final String username = ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");

        return paymentService.initAuctionPayment(auctionId, username);
    }
}


