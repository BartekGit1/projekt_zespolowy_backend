package pl.lodz.p.zesp.payment;

import jakarta.persistence.*;
import pl.lodz.p.zesp.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "premium_subscriptions")
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

    @Column(name = "payment_id", nullable = false)
    private String paymentId;
}
