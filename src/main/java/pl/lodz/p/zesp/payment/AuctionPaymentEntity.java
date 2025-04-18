package pl.lodz.p.zesp.payment;

import jakarta.persistence.*;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.user.UserEntity;

import java.time.LocalDateTime;

public class AuctionPaymentEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private AuctionEntity auction;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
