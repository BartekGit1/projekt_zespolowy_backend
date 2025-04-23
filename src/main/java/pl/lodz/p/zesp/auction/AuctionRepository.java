package pl.lodz.p.zesp.auction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface AuctionRepository extends JpaRepository<AuctionEntity, Long>, JpaSpecificationExecutor<AuctionEntity> {

    @Modifying
    @Query("UPDATE AuctionEntity a SET a.finished = true WHERE a.finished = false AND a.endDate <= :now")
    int markEndedAuctionsAsFinished(LocalDateTime now);
}
