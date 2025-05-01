package pl.lodz.p.zesp.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.zesp.user.UserEntity;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findFirstByUserAndStatusOrderByCreatedAtDesc(UserEntity user, String status);

    List<PaymentEntity> findAllByStatus(String status);
}
