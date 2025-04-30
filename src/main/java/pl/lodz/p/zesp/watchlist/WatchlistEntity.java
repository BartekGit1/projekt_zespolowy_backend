package pl.lodz.p.zesp.watchlist;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.user.UserEntity;

@Entity
@Table(name = "watchlist")
@RequiredArgsConstructor
public class WatchlistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private AuctionEntity auction;

    public WatchlistEntity(UserEntity user, AuctionEntity auction) {
        this.user = user;
        this.auction = auction;
    }

    public UserEntity getUser() {
        return user;
    }

    public AuctionEntity getAuction() {
        return auction;
    }
}
