package pl.lodz.p.zesp.payment;

public interface PaymentService {
    String initPayment(String username);

    void verifyPayment();

    void changeRole();
}
