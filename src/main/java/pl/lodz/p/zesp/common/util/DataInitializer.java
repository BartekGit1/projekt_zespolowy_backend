package pl.lodz.p.zesp.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.auction.AuctionRepository;
import pl.lodz.p.zesp.bid.BidEntity;
import pl.lodz.p.zesp.bid.BidRepository;
import pl.lodz.p.zesp.payment.PaymentEntity;
import pl.lodz.p.zesp.payment.PremiumEntity;
import pl.lodz.p.zesp.payment.PremiumRepository;
import pl.lodz.p.zesp.user.AccountStatus;
import pl.lodz.p.zesp.user.Role;
import pl.lodz.p.zesp.user.UserEntity;
import pl.lodz.p.zesp.user.UserRepository;
import pl.lodz.p.zesp.watchlist.WatchlistEntity;
import pl.lodz.p.zesp.watchlist.WatchlistRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Component
@ConditionalOnProperty(name = "app.data-initializer.enabled", havingValue = "true")
@RequiredArgsConstructor
@Log4j2
public class DataInitializer {

    private final UserRepository userRepository;
    private final PremiumRepository premiumRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final WatchlistRepository watchlistRepository;
    private final Faker faker = new Faker(new Locale("pl"));
    private final SecureRandom random = new SecureRandom();

    private static final int USERS = 50;
    private static final int PREMIUM_SUBSCRIPTIONS = 30;
    private static final int AUCTIONS = 100;
    private static final int WATCHLIST_ITEMS = 150;

    @PostConstruct
    @Transactional
    public void initializeData() {
        log.info("Initializing random data using DataFaker...");
        final var users = createUsers();
        final var premiumSubscriptions = createPremiumSubscriptionsWithPayments(users);
        final var auctions = createAuctionsWithBidsAndPayments(users, premiumSubscriptions);
        createWatchlistItems(users, auctions);
        log.info("Data initialization complete.");
    }

    private List<UserEntity> createUsers() {
        final var users = IntStream.range(0, USERS)
                .mapToObj(i -> {
                    final var firstName = faker.name().firstName();
                    final var lastName = faker.name().lastName();
                    final var username = faker.name().fullName().replaceAll("[^a-zA-Z0-9]", "") + i;
                    final var email = faker.internet().emailAddress(firstName.toLowerCase() + "." + lastName.toLowerCase());
                    return new UserEntity(username, email, AccountStatus.ACTIVE, Role.CUSTOMER);
                })
                .toList();
        final var savedUsers = userRepository.saveAll(users);
        log.info("Created {} users.", savedUsers.size());
        return savedUsers;
    }

    private List<PremiumEntity> createPremiumSubscriptionsWithPayments(List<UserEntity> users) {
        final var premiumSubscriptionsToSave = new ArrayList<PremiumEntity>();
        final var PREMIUM_IN_DAYS = 30;

        IntStream.range(0, PREMIUM_SUBSCRIPTIONS)
                .forEach(i -> {
                    final var randomUser = users.get(random.nextInt(users.size()));
                    final var startDate = getRandomDateTime(PREMIUM_IN_DAYS);
                    final var endDate = startDate.plusDays(PREMIUM_IN_DAYS);
                    final var payment = PaymentEntity.builder()
                            .user(randomUser)
                            .amount(BigDecimal.valueOf(10.00))
                            .status("COMPLETED")
                            .createdAt(startDate)
                            .build();
                    final var premium = new PremiumEntity(randomUser, startDate, endDate, payment);
                    payment.setPremium(premium);
                    premiumSubscriptionsToSave.add(premium);
                });

        final var savedPremiumSubscriptions = premiumRepository.saveAll(premiumSubscriptionsToSave);
        log.info("Created {} premium subscriptions.", savedPremiumSubscriptions.size());
        return savedPremiumSubscriptions;
    }

    private List<AuctionEntity> createAuctionsWithBidsAndPayments(List<UserEntity> users, List<PremiumEntity> premiumSubscriptions) {
        final var auctions = createAuctions(users, premiumSubscriptions);
        createAuctionsBids(auctions, users);
        createAuctionsPayment(auctions);
        final var savedAuctions = auctionRepository.saveAll(auctions);
        log.info("Created {} random auctions.", savedAuctions.size());
        return savedAuctions;
    }

    private List<AuctionEntity> createAuctions(List<UserEntity> users, List<PremiumEntity> premiumSubscriptions) {
        final var auctions = IntStream.range(0, AUCTIONS)
                .mapToObj(i -> {
                    final var randomUser = users.get(random.nextInt(users.size()));
                    final var createdAt = getRandomDateTime(90);
                    final var endDate = createdAt.plusDays(random.nextInt(30) + 7);
                    final var startingPrice = BigDecimal.valueOf(faker.number().randomDouble(2, 10, 100));

                    final var isPromoted = premiumSubscriptions.stream()
                            .anyMatch(premium -> premium.getUser().getId().equals(randomUser.getId()));

                    return AuctionEntity.builder()
                            .version(0L)
                            .title(faker.commerce().productName() + " " + faker.commerce().material())
                            .description(faker.lorem().paragraph())
                            .startingPrice(startingPrice)
                            .endDate(endDate)
                            .user(randomUser)
                            .createdAt(endDate.minusDays(5))
                            .isPromoted(isPromoted)
                            .finished(endDate.isAfter(LocalDateTime.now()))
                            .paid(endDate.isAfter(LocalDateTime.now()))
                            .uri(String.format("https://picsum.photos/id/%d/600/400", random.nextInt(1000) + 1))
                            .build();
                })
                .toList();
        return auctionRepository.saveAll(auctions);
    }

    private void createAuctionsBids(List<AuctionEntity> auctions, List<UserEntity> users) {
        final var bidsToBeSaved = new ArrayList<BidEntity>();
        auctions.forEach(auction -> {
            final var numberOfBids = random.nextInt(10) + 1;
            final var auctionStart = auction.getCreatedAt();
            final var auctionEnd = auction.getEndDate();
            final var timeDifferenceMinutes = Duration.between(auctionStart, auctionEnd).toMinutes();
            final var initialAmount = auction.getStartingPrice();
            final var increment = BigDecimal.valueOf(faker.number().randomDouble(2, 1, 5));

            if (timeDifferenceMinutes > numberOfBids) {
                IntStream.range(0, numberOfBids)
                        .forEach((i) -> {
                            UserEntity randomUser;
                            do {
                                randomUser = users.get(random.nextInt(users.size()));
                            } while (randomUser.getId().equals(auction.getUser().getId()));
                            final var currentAmount = initialAmount.add(increment.multiply(BigDecimal.valueOf(i + 1)));
                            final var bidTime = auctionStart.plusMinutes((i + 1) * (timeDifferenceMinutes / (numberOfBids + 1)));

                            final var bid = BidEntity.builder()
                                    .auction(auction)
                                    .user(randomUser)
                                    .bidTime(bidTime.isBefore(auctionEnd) ? bidTime : auctionEnd.minusMinutes(1))
                                    .amount(currentAmount)
                                    .build();
                            bidsToBeSaved.add(bid);
                            auction.getBids().add(bid);
                        });
            }
        });
        bidRepository.saveAll(bidsToBeSaved);
    }

    private void createAuctionsPayment(List<AuctionEntity> auctions) {
        auctions.stream()
                .filter(auction -> auction.isFinished() && auction.isPaid() && !auction.getBids().isEmpty())
                .forEach(auction -> {
                    final var bid = auction.getBids().getFirst();
                    final var payment = PaymentEntity.builder()
                            .auction(auction)
                            .user(bid.getUser())
                            .amount(bid.getAmount())
                            .status("COMPLETED")
                            .createdAt(auction.getEndDate().plusHours(random.nextInt(48) + 1))
                            .build();
                    auction.setPayment(payment);
                });
    }

    private void createWatchlistItems(List<UserEntity> users, List<AuctionEntity> auctions) {
        final var addedWatchlistItems = new HashSet<String>();
        final var watchlistItemsToSave = new ArrayList<WatchlistEntity>();

        IntStream.range(0, WATCHLIST_ITEMS)
                .forEach(i -> {
                    final var randomUser = users.get(random.nextInt(users.size()));
                    final var randomAuction = auctions.get(random.nextInt(auctions.size()));

                    if (!randomUser.getId().equals(randomAuction.getUser().getId())) {
                        final var userAuctionKey = randomUser.getId() + "-" + randomAuction.getId();
                        if (!addedWatchlistItems.contains(userAuctionKey)) {
                            watchlistItemsToSave.add(new WatchlistEntity(randomUser, randomAuction));
                            addedWatchlistItems.add(userAuctionKey);
                        }
                    }
                });
        final var savedWatchlistItems = watchlistRepository.saveAll(watchlistItemsToSave);
        log.info("Created {} random watchlist items.", savedWatchlistItems.size());
    }

    private LocalDateTime getRandomDateTime(final int days) {
        return LocalDateTime.ofInstant(faker.timeAndDate().past(days, TimeUnit.DAYS), ZoneId.of("Europe/Warsaw"));
    }
}