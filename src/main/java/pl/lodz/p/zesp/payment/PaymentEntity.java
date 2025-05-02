package pl.lodz.p.zesp.payment;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.lodz.p.zesp.auction.AuctionEntity;
import pl.lodz.p.zesp.user.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Builder
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status = "IN_PROGRESS";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "payment")
    private AuctionEntity auction;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType type;

    public UserEntity getUser() {
        return user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public AuctionEntity getAuction() {
        return auction;
    }

    public PaymentEntity(UserEntity user, BigDecimal amount, PaymentType type) {
        this.user = user;
        this.amount = amount;
        this.type = type;
    }
}
