package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum BnpParibasFortisPaymentStatus {
    ACCEPTED_CUSTOMER_PROFILE("ACCP", PaymentStatus.PENDING),
    ACCEPTED_SETTLEMENT_COMPLETED("ACSC", PaymentStatus.PAID),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC", PaymentStatus.PENDING),
    ACCEPTED_WITH_CHANGE("ACWC", PaymentStatus.PENDING),
    ACCEPTED_WITHOUT_POSTING("ACWP", PaymentStatus.PENDING),
    PARTIALLY_ACCEPTED("PART", PaymentStatus.PENDING),
    RECEIVED("RCVD", PaymentStatus.CREATED),
    PENDING("PNDG", PaymentStatus.PENDING),
    REJECTED("RJCT", PaymentStatus.PENDING),
    UNKNOWN("UNKNOWN", PaymentStatus.UNDEFINED);

    private final PaymentStatus paymentStatus;
    private final String text;

    BnpParibasFortisPaymentStatus(String text, PaymentStatus paymentStatus) {
        this.text = text;
        this.paymentStatus = paymentStatus;
    }

    public PaymentStatus getTinkPaymentStatus() {
        return paymentStatus;
    }

    @Override
    public String toString() {
        return text;
    }

    public static BnpParibasFortisPaymentStatus fromString(String text) {
        return Arrays.stream(BnpParibasFortisPaymentStatus.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
