package pl.lodz.p.zesp.bid;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.user.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class BidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private AuctionEntity auction;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;
}
