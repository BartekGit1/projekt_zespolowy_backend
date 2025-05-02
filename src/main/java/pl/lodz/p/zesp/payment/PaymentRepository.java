package pl.lodz.p.zesp.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.zesp.user.UserEntity;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findFirstByUserAndStatusAndTypeOrderByCreatedAtDesc(UserEntity user, String status, PaymentType type);

    List<PaymentEntity> findAllByStatusAndType(String status, PaymentType type);
}
