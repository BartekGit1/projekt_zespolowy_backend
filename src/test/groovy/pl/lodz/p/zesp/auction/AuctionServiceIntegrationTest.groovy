package pl.lodz.p.zesp.auction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.lodz.p.zesp.auction.dto.AuctionFilter
import pl.lodz.p.zesp.auction.dto.AuctionStatus
import pl.lodz.p.zesp.bid.BidEntity
import pl.lodz.p.zesp.user.AccountStatus
import pl.lodz.p.zesp.user.Role
import pl.lodz.p.zesp.user.UserEntity
import pl.lodz.p.zesp.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime

import static java.time.temporal.ChronoUnit.SECONDS

@SpringBootTest
@ActiveProfiles("integration-testing")
class AuctionServiceIntegrationTest extends Specification {

	@Autowired
	AuctionService auctionService
	@Autowired
	AuctionRepository auctionRepository
	@Autowired
	UserRepository userRepository

	@Shared
	UserEntity user1
	@Shared
	UserEntity user2

	@Shared
	AuctionEntity auction1
	@Shared
	AuctionEntity auction2

	def setup() {
		user1 = userRepository.save(new UserEntity("user1", "user1@email.com", AccountStatus.ACTIVE, Role.CUSTOMER))
		user2 = userRepository.save(new UserEntity("user2", "user2@email.com", AccountStatus.ACTIVE, Role.CUSTOMER))
		setupAuctions()
	}

	def cleanup() {
		auctionRepository.deleteAll()
		userRepository.deleteAll()
	}

	private def setupAuctions() {
		def now = LocalDateTime.now().truncatedTo(SECONDS)
		auction1 = AuctionEntity.builder()
				.title(VINTAGE_WATCH_TITLE)
				.description(CLASSIC_TIMEPIECE_DESCRIPTION)
				.startingPrice(VINTAGE_WATCH_PRICE)
				.endDate(now.plusDays(7))
				.user(user1)
				.isPromoted(true)
				.finished(false)
				.bids(new ArrayList<BidEntity>())
				.build()

		auction2 = AuctionEntity.builder()
				.title(ANTIQUE_BOOK_TITLE)
				.description(FIRST_EDITION_NOVEL_DESCRIPTION)
				.startingPrice(ANTIQUE_BOOK_PRICE)
				.endDate(now.plusWeeks(2))
				.user(user2)
				.isPromoted(false)
				.finished(true)
				.bids(new ArrayList<BidEntity>())
				.build()
		auctionRepository.saveAll([auction1, auction2])
	}

	def "getAuctions should filter by status ACTIVE"() {
		given:
		def filter = AuctionFilter.builder().status(AuctionStatus.ACTIVE).build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.title() == VINTAGE_WATCH_TITLE }
	}

	def "getAuctions should filter by status FINISHED"() {
		given:
		def filter = AuctionFilter.builder().status(AuctionStatus.FINISHED).build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.title() == ANTIQUE_BOOK_TITLE }
	}

	def "getAuctions should filter by title"() {
		given:
		def filter = AuctionFilter.builder().title("Vintage").build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.title() == VINTAGE_WATCH_TITLE }
	}

	def "getAuctions should filter by description"() {
		given:
		def filter = AuctionFilter.builder().description("novel").build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.description() == FIRST_EDITION_NOVEL_DESCRIPTION }
	}

	def "getAuctions should filter by promoted = true"() {
		given:
		def filter = AuctionFilter.builder().promoted(true).build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.isPromoted() }
	}

	def "getAuctions should filter by userId"() {
		given:
		def filter = AuctionFilter.builder().userId(user1.getId()).build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.user().id() == user1.getId() }
	}

	def "getAuctions should filter by endDateBefore"() {
		given:
		def filter = AuctionFilter.builder().endDateBefore(auction2.getEndDate()).build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.id() == auction1.getId() }
	}

	def "getAuctions should filter by endDateAfter"() {
		given:
		def filter = AuctionFilter.builder().endDateAfter(auction1.getEndDate()).build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.id() == auction2.getId() }
	}

	def "getAuctions should filter by minPrice"() {
		given:
		def filter = AuctionFilter.builder().minPrice(new BigDecimal("75.00")).build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.startingPrice() == VINTAGE_WATCH_PRICE }
	}

	def "getAuctions should filter by maxPrice"() {
		given:
		def filter = AuctionFilter.builder().maxPrice(new BigDecimal("75.00")).build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.startingPrice() == ANTIQUE_BOOK_PRICE }
	}

	def "getAuctions should filter by multiple criteria"() {
		given:
		def filter = AuctionFilter.builder()
				.status(AuctionStatus.ACTIVE)
				.title("Vintage")
				.promoted(true)
				.userId(user1.getId())
				.minPrice(new BigDecimal("90.00"))
				.maxPrice(new BigDecimal("110.00"))
				.build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 1
		result.any { it.title() == VINTAGE_WATCH_TITLE && it.isPromoted() && it.user().id() == user1.getId() && it.startingPrice() == VINTAGE_WATCH_PRICE }
	}

	def "getAuctions should return auctions sorted by isFinished (false first) and then by endDate ascending"() {
		given:
		def now = LocalDateTime.now().truncatedTo(SECONDS)
		def auction3 = AuctionEntity.builder()
				.title("Auction C")
				.description("Description 2")
				.startingPrice(new BigDecimal("30.00"))
				.endDate(now.plusDays(1))
				.user(user1)
				.finished(false)
				.bids(new ArrayList<BidEntity>())
				.build()

		def auction4 = AuctionEntity.builder()
				.title("Auction D")
				.description("Description 2")
				.startingPrice(new BigDecimal("40.00"))
				.endDate(now.plusDays(5))
				.user(user2)
				.finished(true)
				.bids(new ArrayList<BidEntity>())
				.build()

		auctionRepository.saveAll([auction3, auction4])
		def filter = AuctionFilter.builder().build()

		when:
		def result = auctionService.getAuctions(filter)

		then:
		result.size() == 4
		result[0].id() == auction3.getId()
		result[1].id() == auction1.getId()
		result[2].id() == auction4.getId()
		result[3].id() == auction2.getId()
	}

	private static final String VINTAGE_WATCH_TITLE = "Vintage Watch"
	private static final String CLASSIC_TIMEPIECE_DESCRIPTION = "Classic timepiece"
	private static final BigDecimal VINTAGE_WATCH_PRICE = new BigDecimal("100.00")
	private static final String ANTIQUE_BOOK_TITLE = "Antique Book"
	private static final String FIRST_EDITION_NOVEL_DESCRIPTION = "First edition novel"
	private static final BigDecimal ANTIQUE_BOOK_PRICE = new BigDecimal("50.00")
}