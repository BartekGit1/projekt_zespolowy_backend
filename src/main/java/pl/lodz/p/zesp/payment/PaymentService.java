package pl.lodz.p.zesp.payment;

public interface PaymentService {
    String initPayment(String username);

    String initAuctionPayment(Long auctionId, String username);

    void verifyPayment();

    void changeRole();

    void verifyAuctionPayment();
}
