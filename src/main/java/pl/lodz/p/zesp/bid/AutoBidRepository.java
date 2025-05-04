package pl.lodz.p.zesp.bid;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.user.UserEntity;

import java.util.List;
import java.util.Optional;

public interface AutoBidRepository extends JpaRepository<AutoBidEntity, Long> {
    List<AutoBidEntity> findAllByAuctionAndActiveTrue(AuctionEntity auction);

    Optional<AutoBidEntity> findByAuctionAndUser(AuctionEntity auction, UserEntity user);
}
