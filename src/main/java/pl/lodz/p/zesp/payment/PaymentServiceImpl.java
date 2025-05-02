package pl.lodz.p.zesp.payment;

import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentIntentSearchResult;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentSearchParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.auction.AuctionRepository;
import pl.lodz.p.zesp.bid.BidEntity;
import pl.lodz.p.zesp.bid.BidRepository;
import pl.lodz.p.zesp.common.util.api.exception.CustomException;
import pl.lodz.p.zesp.payment.dto.response.ClientSecretResponseDto;
import pl.lodz.p.zesp.user.Role;
import pl.lodz.p.zesp.user.UserEntity;
import pl.lodz.p.zesp.user.UserRepository;
import pl.lodz.p.zesp.user.UserService;
import pl.lodz.p.zesp.user.exception.exceptions.UserNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.lodz.p.zesp.common.util.Constants.*;

@Service
class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PremiumRepository premiumRepository;
    private final UserService userService;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Value("${stripe.apikey}")
    private String stripeApiKey;

    protected static final Logger LOGGER = Logger.getGlobal();

    public PaymentServiceImpl(UserRepository userRepository, PaymentRepository paymentRepository,
                              PremiumRepository premiumRepository, UserService userService,
                              AuctionRepository auctionRepository, BidRepository bidRepository) {
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.premiumRepository = premiumRepository;
        this.userService = userService;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
    }

    @Transactional
    @Override
    public String initPayment(String username) {
        return handleInitPayment(
                username,
                PaymentType.PREMIUM,
                null,
                id -> createPaymentIntentParams(findUser(username), id, BigDecimal.valueOf(PREMIUM_CUSTOMER_PRICE), "paymentId")
        );
    }

    @Transactional
    @Override
    public String initAuctionPayment(Long auctionId, String username) {
        AuctionEntity auction = auctionRepository.findById(auctionId)
                .filter(AuctionEntity::isFinished)
                .orElse(null);

        if (auction == null) {
            return "";
        }

        BigDecimal amount = bidRepository.findFirstByAuctionOrderByBidTimeDesc(auction)
                .map(BidEntity::getAmount)
                .orElseThrow(() -> new CustomException("No bids found for auction"));

        return handleInitPayment(
                username,
                PaymentType.AUCTION,
                auction,
                id -> createPaymentIntentParams(findUser(username), auction.getId(), amount, "auctionId")
        );
    }

    private String handleInitPayment(String username, PaymentType type, AuctionEntity auction, Function<Long, PaymentIntentCreateParams> paramProvider) {
        Stripe.apiKey = stripeApiKey;
        UserEntity user = findUser(username);

        Optional<PaymentEntity> existing = paymentRepository
                .findFirstByUserAndStatusAndTypeOrderByCreatedAtDesc(user, PaymentStatus.IN_PROGRESS.toString(), type);

        return existing.map(payment ->
                searchForPayment(STRIPE_REQUIRES_PAYMENT_STATUS, payment.getId(), type)
                        .getData().stream().findFirst()
                        .map(intent -> new Gson().toJson(new ClientSecretResponseDto(intent.getClientSecret())))
                        .orElseGet(() -> createAndReturnClientSecret(user, type, auction, paramProvider))
        ).orElseGet(() -> createAndReturnClientSecret(user, type, auction, paramProvider));
    }

    private String createAndReturnClientSecret(UserEntity user, PaymentType type, AuctionEntity auction, Function<Long, PaymentIntentCreateParams> paramProvider) {
        BigDecimal amount = (type == PaymentType.PREMIUM) ? BigDecimal.valueOf(PREMIUM_CUSTOMER_PRICE) :
                bidRepository.findFirstByAuctionOrderByBidTimeDesc(auction).map(BidEntity::getAmount)
                        .orElseThrow(() -> new CustomException("No bid found"));

        PaymentEntity payment = new PaymentEntity(user, amount, type);
        paymentRepository.save(payment);

        if (type == PaymentType.AUCTION && auction != null) {
            auction.setPayment(payment);
            auctionRepository.save(auction);
        }

        try {
            PaymentIntent intent = PaymentIntent.create(paramProvider.apply(payment.getId()));
            LOGGER.log(Level.INFO, "Payment created for user: {0}", user.getUsername());
            return new Gson().toJson(new ClientSecretResponseDto(intent.getClientSecret()));
        } catch (StripeException e) {
            throw new CustomException("Payment error occurred");
        }
    }

    private PaymentIntentCreateParams createPaymentIntentParams(UserEntity user, Long id, BigDecimal amount, String metadataKey) {
        long stripeAmount = amount.multiply(STRIPE_MULTIPLIER).longValue() * 4;
        return PaymentIntentCreateParams.builder()
                .setAmount(stripeAmount)
                .setCurrency("PLN")
                .setReceiptEmail(user.getEmail())
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                .putMetadata(metadataKey, id.toString())
                .build();
    }

    @Transactional
    @Override
    public void verifyPayment() {
        verifyGenericPayments(PaymentType.PREMIUM, 7, (payment, user) -> {
            final LocalDateTime now = LocalDateTime.now();
            premiumRepository.save(new PremiumEntity(user, now, now.plusMonths(1), payment));
            user.setRole(Role.PREMIUM);
            userRepository.save(user);
            userService.updateRolePayment(user.getUsername(), Role.PREMIUM);
            LOGGER.log(Level.INFO, "User {0} upgraded to PREMIUM", user.getUsername());
        });
    }

    @Transactional
    @Override
    public void verifyAuctionPayment() {
        verifyGenericPayments(PaymentType.AUCTION, 14, (payment, user) ->
                auctionRepository.findByPaymentId(payment.getId()).ifPresent(auction -> {
                    auction.setPaid(true);
                    auctionRepository.save(auction);
                    LOGGER.log(Level.INFO, "Confirmed auction for payment: {0}", auction.getId());
                }));
    }

    private void verifyGenericPayments(PaymentType type, int expiryDays, PaymentHandler handler) {
        Stripe.apiKey = stripeApiKey;

        List<PaymentEntity> payments = paymentRepository.findAllByStatusAndType(PaymentStatus.IN_PROGRESS.toString(), type);

        for (PaymentEntity payment : payments) {
            UserEntity user = payment.getUser();
            boolean isPaid = searchForPayment(STRIPE_PAID_STATUS, type == PaymentType.PREMIUM ? payment.getId() :
                    auctionRepository.findByPaymentId(payment.getId()).map(AuctionEntity::getId).orElse(null), type)
                    .getData().stream().findFirst().isPresent();

            if (isPaid) {
                payment.setStatus(PaymentStatus.COMPLETED.toString());
                paymentRepository.save(payment);
                handler.handle(payment, user);
            } else if (payment.getCreatedAt().plusDays(expiryDays).isBefore(LocalDateTime.now())) {
                paymentRepository.delete(payment);
                LOGGER.log(Level.INFO, "{0} payment expired and removed for user {1}", new Object[]{type, user.getUsername()});
            }
        }
    }

    private PaymentIntentSearchResult searchForPayment(String status, Long id, PaymentType type) {
        try {
            String metadataKey = (type == PaymentType.PREMIUM) ? "paymentId" : "auctionId";
            PaymentIntentSearchParams params = PaymentIntentSearchParams.builder()
                    .setQuery("status:'" + status + "' AND metadata['" + metadataKey + "']:'" + id + "'")
                    .build();
            return PaymentIntent.search(params);
        } catch (StripeException e) {
            throw new CustomException("Stripe error occurred");
        }
    }

    private UserEntity findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    @Transactional
    public void changeRole() {
        userRepository.findAllByRole(Role.PREMIUM).forEach(user ->
                premiumRepository.findTopByUserOrderByEndDateDesc(user)
                        .filter(premium -> premium.getEndDate().isBefore(LocalDateTime.now()))
                        .ifPresent(premium -> {
                            user.setRole(Role.CUSTOMER);
                            userRepository.save(user);
                            userService.updateRolePayment(user.getUsername(), Role.CUSTOMER);
                            LOGGER.log(Level.INFO, "User {0} role changed to CUSTOMER", user.getUsername());
                        })
        );
    }

    private interface PaymentHandler {
        void handle(PaymentEntity payment, UserEntity user);
    }
}
