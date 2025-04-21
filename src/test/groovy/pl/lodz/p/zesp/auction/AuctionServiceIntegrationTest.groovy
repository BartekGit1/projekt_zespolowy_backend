package pl.lodz.p.zesp.auction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import pl.lodz.p.zesp.auction.dto.AuctionFilter
import pl.lodz.p.zesp.auction.dto.AuctionResponse
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

	def "getAuctions should filter by status ACTIVE and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().status(AuctionStatus.ACTIVE).build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.title() == VINTAGE_WATCH_TITLE }
	}

	def "getAuctions should filter by status FINISHED and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().status(AuctionStatus.FINISHED).build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.title() == ANTIQUE_BOOK_TITLE }
	}

	def "getAuctions should filter by title and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().title("Vintage").build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.title() == VINTAGE_WATCH_TITLE }
	}

	def "getAuctions should filter by description and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().description("novel").build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.description() == FIRST_EDITION_NOVEL_DESCRIPTION }
	}

	def "getAuctions should filter by promoted = true and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().promoted(true).build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.isPromoted() }
	}

	def "getAuctions should filter by userId and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().userId(user1.getId()).build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.user().id() == user1.getId() }
	}

	def "getAuctions should filter by endDateBefore and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().endDateBefore(auction2.getEndDate()).build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.id() == auction1.getId() }
	}

	def "getAuctions should filter by endDateAfter and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().endDateAfter(auction1.getEndDate()).build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.id() == auction2.getId() }
	}

	def "getAuctions should filter by minPrice and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().minPrice(new BigDecimal("75.00")).build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.startingPrice() == VINTAGE_WATCH_PRICE }
	}

	def "getAuctions should filter by maxPrice and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder().maxPrice(new BigDecimal("75.00")).build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.startingPrice() == ANTIQUE_BOOK_PRICE }
	}

	def "getAuctions should filter by multiple criteria and return a Page of AuctionResponse"() {
		given:
		def filter = AuctionFilter.builder()
				.status(AuctionStatus.ACTIVE)
				.title("Vintage")
				.promoted(true)
				.userId(user1.getId())
				.minPrice(new BigDecimal("90.00"))
				.maxPrice(new BigDecimal("110.00"))
				.build()
		def pageable = PageRequest.of(0, 10)

		when:
		Page<AuctionResponse> resultPage = auctionService.getAuctions(filter, pageable)
		def result = resultPage.getContent()

		then:
		resultPage.getTotalElements() == 1
		result.size() == 1
		result.any { it.title() == VINTAGE_WATCH_TITLE && it.isPromoted() && it.user().id() == user1.getId() && it.startingPrice() == VINTAGE_WATCH_PRICE }
	}
	
	private static final String VINTAGE_WATCH_TITLE = "Vintage Watch"
	private static final String CLASSIC_TIMEPIECE_DESCRIPTION = "Classic timepiece"
	private static final BigDecimal VINTAGE_WATCH_PRICE = new BigDecimal("100.00")
	private static final String ANTIQUE_BOOK_TITLE = "Antique Book"
	private static final String FIRST_EDITION_NOVEL_DESCRIPTION = "First edition novel"
	private static final BigDecimal ANTIQUE_BOOK_PRICE = new BigDecimal("50.00")
}