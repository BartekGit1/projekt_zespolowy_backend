package pl.lodz.p.zesp.auction;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.lodz.p.zesp.bid.BidEntity;
import pl.lodz.p.zesp.payment.PaymentEntity;
import pl.lodz.p.zesp.user.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auctions")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuctionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "starting_price", nullable = false)
    private BigDecimal startingPrice;

    @OneToMany(mappedBy = "auction", fetch = FetchType.EAGER)
    @OrderBy("amount desc")
    private List<BidEntity> bids = new ArrayList<>();

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "is_promoted", nullable = false)
    private boolean isPromoted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean finished = false;

    @Column(nullable = false)
    private boolean paid = false;

    private String uri;

    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;
}
