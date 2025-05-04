package pl.lodz.p.zesp.common.util;

import java.math.BigDecimal;

public class Constants {
    public static final String URI_VERSION_V1 = "/api/v1";
    public static final String URI_REGISTER = "/register";
    public static final String URI_USERS = "/users";
    public static final String URI_PAYMENTS = "/payments";
    public static final BigDecimal STRIPE_MULTIPLIER = new BigDecimal("100");
    public static final Long PREMIUM_CUSTOMER_PRICE = 10L;
    public static final String STRIPE_REQUIRES_PAYMENT_STATUS = "requires_payment_method";
    public static final String STRIPE_PAID_STATUS = "Succeeded";
    public static final BigDecimal BID_LIMIT = new BigDecimal("99999999.99");
    private Constants() {
    }
}
