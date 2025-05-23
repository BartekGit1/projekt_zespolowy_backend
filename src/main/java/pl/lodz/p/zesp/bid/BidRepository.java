package pl.lodz.p.zesp.bid;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.lodz.p.zesp.auction.AuctionEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<BidEntity, Long> {

    @Query("""
        SELECT 
            FUNCTION('TO_CHAR', FUNCTION('DATE_TRUNC', 'day', b.bidTime), 'YYYY-MM-DD') AS date,
            COUNT(b) AS count
        FROM BidEntity b
        WHERE b.bidTime >= :fromDate
        GROUP BY FUNCTION('DATE_TRUNC', 'day', b.bidTime)
        ORDER BY FUNCTION('DATE_TRUNC', 'day', b.bidTime)
    """)
    List<BidHistogramEntry> findHistogramFromDate(LocalDateTime fromDate);


    Optional<BidEntity> findFirstByAuctionOrderByBidTimeDesc(AuctionEntity auctionEntity);
}
