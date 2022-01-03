package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum DnbPaymentStatus {
    ACCEPTED("ACCP", PaymentStatus.PAID),
    ACTIVE("ACTV", PaymentStatus.PENDING),
    STOPPED("PRSY", PaymentStatus.CANCELLED),
    PROCESSING("NEXT", PaymentStatus.PENDING),
    PENDING("PDNG", PaymentStatus.PENDING),
    REJECTED("RJCT", PaymentStatus.REJECTED),
    EXECUTED("ACSC", PaymentStatus.PAID),
    NAUT("NAUT", PaymentStatus.PENDING),
    UNDEFINED("Undefined", PaymentStatus.UNDEFINED);

    private String text;
    private PaymentStatus paymentStatus;

    DnbPaymentStatus(String text, PaymentStatus paymentStatus) {
        this.text = text;
        this.paymentStatus = paymentStatus;
    }

    public static DnbPaymentStatus fromString(String text) {
        return Arrays.stream(DnbPaymentStatus.values())
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
