package pl.lodz.p.zesp.bid;

import jakarta.persistence.*;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.user.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auto_bids")
public class AutoBidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private AuctionEntity auction;

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active = true;

    public UserEntity getUser() {
        return user;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public AutoBidEntity() {
    }

    public AutoBidEntity(UserEntity user, AuctionEntity auction, BigDecimal maxAmount, LocalDateTime createdAt) {
        this.user = user;
        this.auction = auction;
        this.maxAmount = maxAmount;
        this.createdAt = createdAt;
    }
}
