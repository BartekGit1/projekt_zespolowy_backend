package pl.lodz.p.zesp.auction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuctionRepository extends JpaRepository<AuctionEntity, Long>, JpaSpecificationExecutor<AuctionEntity> {
}
