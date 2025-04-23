package pl.lodz.p.zesp.auction;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuctionJobScheduler {

    private final AuctionRepository auctionRepository;

    @Scheduled(fixedRate = 1000) // 1s
    @Transactional
    public void processEndedAuctions() {
        int updatedCount = auctionRepository.markEndedAuctionsAsFinished(LocalDateTime.now());
        if (updatedCount > 0) {
            log.info("Marked {} auctions as finished", updatedCount);
        }
    }
}
