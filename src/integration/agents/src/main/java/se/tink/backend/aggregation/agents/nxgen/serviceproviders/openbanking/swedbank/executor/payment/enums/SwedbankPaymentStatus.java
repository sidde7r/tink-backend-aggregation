package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum SwedbankPaymentStatus {
    REJECTED("RJCT", PaymentStatus.REJECTED),
    CANCELED("CANC", PaymentStatus.CANCELLED),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC", PaymentStatus.PENDING),
    PARTIALLY_ACCEPTED_TECHNICAL_VALIDATION("PATC", PaymentStatus.PENDING),
    ACCEPTER_CUSTOMER_PROFILE("ACCP", PaymentStatus.PENDING),
    ACCEPTER_SETTLEMENT_COMPLETED_DEBTOR("ACSC", PaymentStatus.PAID),
    ACCEPTER_SETTLEMENT_COMPLETED_CREDITOR("ACCC", PaymentStatus.PAID),
    UNDEFINED("Undefined", PaymentStatus.UNDEFINED);

    private String text;
    private PaymentStatus paymentStatus;

    SwedbankPaymentStatus(String text, PaymentStatus paymentStatus) {
        this.text = text;
        this.paymentStatus = paymentStatus;
    }

    public static SwedbankPaymentStatus fromString(String text) {
        return Arrays.stream(SwedbankPaymentStatus.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNDEFINED);
    }

    public PaymentStatus getTinkPaymentStatus() {
        return paymentStatus;
    }

    @Override
    public String toString() {
        return text;
    }
}
