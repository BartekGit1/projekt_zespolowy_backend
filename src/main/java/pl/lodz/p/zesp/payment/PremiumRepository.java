package pl.lodz.p.zesp.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.zesp.user.UserEntity;

import java.util.Optional;

public interface PremiumRepository extends JpaRepository<PremiumEntity, Long> {
    Optional<PremiumEntity> findTopByUserOrderByEndDateDesc(UserEntity user);
}
