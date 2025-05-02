package pl.lodz.p.zesp.auction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.lodz.p.zesp.auction.dto.AuctionResponse;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<AuctionEntity, Long>, JpaSpecificationExecutor<AuctionEntity> {

    @Modifying
    @Query("UPDATE AuctionEntity a SET a.finished = true WHERE a.finished = false AND a.endDate <= :now")
    int markEndedAuctionsAsFinished(LocalDateTime now);

    @Query("SELECT a FROM AuctionEntity a JOIN FETCH a.user u WHERE u.username=:username")
    Page<AuctionResponse> findAllMyAuctions(@Param(value = "username") String username,final Pageable pageable);

    @Query("""
                       SELECT a FROM AuctionEntity a\s
                          JOIN FETCH a.user u
                          JOIN FETCH a.bids b\s
                          WHERE  b.user.username = :username\s
                          AND b.amount = (SELECT MAX(b2.amount) FROM BidEntity b2 WHERE b2.auction.id = a.id)
            """)
    Page<AuctionResponse> findAllMyWonAuctions(@Param(value = "username") String username,final Pageable pageable);

    @Query("""
                       SELECT a FROM AuctionEntity a\s
                          JOIN FETCH a.user u
                          WHERE a.finished = true\
            """)
    Page<AuctionResponse> findAllFinishedAuctions(final Pageable pageable);

    Optional<AuctionEntity> findByPaymentId(Long paymentId);
}
