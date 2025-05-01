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
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.lodz.p.zesp.common.util.Constants.*;

@Service
class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PremiumRepository premiumRepository;
    private final UserService userService;

    @Value("${stripe.apikey}")
    private String stripeApiKey;

    protected static final Logger LOGGER = Logger.getGlobal();

    public PaymentServiceImpl(UserRepository userRepository, PaymentRepository paymentRepository, PremiumRepository premiumRepository, UserService userService) {
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.premiumRepository = premiumRepository;
        this.userService = userService;
    }

    @Transactional
    @Override
    public String initPayment(String username) {
        Stripe.apiKey = stripeApiKey;

        final UserEntity user = findUser(username);
        final Optional<PaymentEntity> existingPayment = paymentRepository
                .findFirstByUserAndStatusOrderByCreatedAtDesc(user, PaymentStatus.IN_PROGRESS.toString());

        if (existingPayment.isPresent()) {
            return searchForPayment(STRIPE_REQUIRES_PAYMENT_STATUS, existingPayment.get().getId())
                    .getData()
                    .stream()
                    .findFirst()
                    .map(paymentIntent -> new Gson().toJson(new ClientSecretResponseDto(paymentIntent.getClientSecret())))
                    .orElseGet(() -> createPayment(user));
        } else {
            return createPayment(user);
        }
    }

    private String createPayment(UserEntity user) {
        PaymentEntity paymentEntity = new PaymentEntity(user, BigDecimal.valueOf(PREMIUM_CUSTOMER_PRICE));
        paymentRepository.save(paymentEntity);

        final PaymentIntentCreateParams params = preparePaymentIntentParams(user, paymentEntity.getId());
        try {
            final PaymentIntent paymentIntent = PaymentIntent.create(params);

            LOGGER.log(Level.INFO, "Payment created for user: {0}", user.getUsername());
            return new Gson().toJson(new ClientSecretResponseDto(paymentIntent.getClientSecret()));
        } catch (StripeException e) {
            throw new CustomException("Payment error occurred");
        }
    }

    private PaymentIntentCreateParams preparePaymentIntentParams(UserEntity user, Long id) {
        final Long price = PREMIUM_CUSTOMER_PRICE * STRIPE_MULTIPLIER;
        return PaymentIntentCreateParams.builder()
                .setAmount(price)
                .setCurrency("PLN")
                .setReceiptEmail(user.getEmail())
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                .putMetadata("paymentId", id.toString())
                .build();
    }

    @Transactional
    @Override
    public void verifyPayment() {
        Stripe.apiKey = stripeApiKey;

        List<PaymentEntity> payments = paymentRepository.findAllByStatus(PaymentStatus.IN_PROGRESS.toString());

        for (PaymentEntity payment : payments) {
            final UserEntity user = payment.getUser();

            boolean isPaid = searchForPayment(STRIPE_PAID_STATUS, payment.getId())
                    .getData()
                    .stream()
                    .findFirst()
                    .isPresent();

            if (isPaid) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endDate = now.plusMonths(1);

                PremiumEntity premium = new PremiumEntity(user, now, endDate, payment);
                premiumRepository.save(premium);

                user.setRole(Role.PREMIUM);
                userRepository.save(user);

                payment.setStatus(PaymentStatus.COMPLETED.toString());
                paymentRepository.save(payment);

                userService.updateRolePayment(user.getUsername(), Role.PREMIUM);

                LOGGER.log(Level.INFO, "User {0} upgraded to PREMIUM", user.getUsername());
            } else if (payment.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now())) {
                paymentRepository.delete(payment);
                LOGGER.log(Level.INFO, "Payment expired and removed for user {0}", user.getUsername());
            }
        }
    }

    private PaymentIntentSearchResult searchForPayment(String status, Long id) {
        try {
            PaymentIntentSearchParams params = PaymentIntentSearchParams.builder()
                    .setQuery("status:'" + status + "' AND metadata['paymentId']:'" + id + "'")
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
        final List<UserEntity> userEntityList = userRepository.findAllByRole(Role.PREMIUM);
        for (UserEntity user : userEntityList) {
            Optional<PremiumEntity> latestPremium = premiumRepository
                    .findTopByUserOrderByEndDateDesc(user);

            if (latestPremium.isPresent() && latestPremium.get().getEndDate().isBefore(LocalDateTime.now())) {
                user.setRole(Role.CUSTOMER);
                userRepository.save(user);
                userService.updateRolePayment(user.getUsername(), Role.CUSTOMER);

                LOGGER.log(Level.INFO, "User {0} role changed to CUSTOMER", user.getUsername());
            }
        }
    }
}