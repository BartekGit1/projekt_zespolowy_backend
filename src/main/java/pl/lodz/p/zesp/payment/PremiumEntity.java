package pl.lodz.p.zesp.payment;

import jakarta.persistence.*;
import lombok.Getter;
import pl.lodz.p.zesp.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "premium_subscriptions")
@Getter
public class PremiumEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    public PremiumEntity() {}

    public PremiumEntity(UserEntity user, LocalDateTime startDate, LocalDateTime endDate, PaymentEntity payment) {
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.payment = payment;
    }
}
