package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum HandelsbankenPaymentStatus {
    REJECTED("RJCT", PaymentStatus.REJECTED),
    CANCELED("CANC", PaymentStatus.CANCELLED),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC", PaymentStatus.PENDING),
    PARTIALLY_ACCEPTED_TECHNICAL_VALIDATION("PATC", PaymentStatus.PENDING),
    ACCEPTER_CUSTOMER_PROFILE("ACCP", PaymentStatus.PENDING),
    ACCEPTER_SETTLEMENT_COMPLETED_DEBTOR("ACSC", PaymentStatus.PAID),
    ACCEPTER_SETTLEMENT_COMPLETED_CREDITOR("ACCC", PaymentStatus.PAID),
    UNDEFINED("Undefined", PaymentStatus.UNDEFINED);

    private final String text;
    private final PaymentStatus paymentStatus;

    HandelsbankenPaymentStatus(String text, PaymentStatus paymentStatus) {
        this.text = text;
        this.paymentStatus = paymentStatus;
    }

    public static HandelsbankenPaymentStatus fromString(String text) {
        return Arrays.stream(HandelsbankenPaymentStatus.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNDEFINED);
    }

    @Override
    public String toString() {
        return text;
    }

    public PaymentStatus getTinkPaymentStatus() {
        return paymentStatus;
    }
}
