package pl.lodz.p.zesp.payment;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class PaymentScheduler {

    private final PaymentService paymentService;

    public PaymentScheduler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Scheduled(fixedRate = 60000)
    public void verifyPayments() {
        paymentService.verifyPayment();
    }

    @Scheduled(fixedRate = 60000)
    public void changeRole() {
        paymentService.changeRole();
    }
}
