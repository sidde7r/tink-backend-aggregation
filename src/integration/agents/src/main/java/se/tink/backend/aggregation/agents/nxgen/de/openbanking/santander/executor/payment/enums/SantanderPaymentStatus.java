package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum SantanderPaymentStatus {
    RECEIVED("RCVD", PaymentStatus.PENDING),
    CANCELED("CANC", PaymentStatus.CANCELLED),
    PARTIALLY_ACCEPTED_TECHNICAL("PATC", PaymentStatus.PENDING),
    ACCEPTED_SETTLEMENT_IN_PROCESS("ACSP", PaymentStatus.PENDING),
    ACCEPTED_WITH_CHANGE("ACWC", PaymentStatus.CREATED),
    PENDING("PDNG", PaymentStatus.PENDING),
    REJECTED("RJCT", PaymentStatus.REJECTED),
    ACCEPTED_SETTLEMENT_COMPLETED("ACSC", PaymentStatus.CREATED),
    ACCEPTED_CUSTOMER_PROFILE("ACCP", PaymentStatus.PENDING),
    UNKNOWN("Unknown", PaymentStatus.UNDEFINED);

    private String statusText;
    private PaymentStatus paymentStatus;

    SantanderPaymentStatus(String status, PaymentStatus paymentStatus) {
        this.statusText = status;
        this.paymentStatus = paymentStatus;
    }

    public static SantanderPaymentStatus fromString(String text) {
        return Arrays.stream(SantanderPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public String getText() {
        return this.statusText;
    }

    @Override
    public String toString() {
        return statusText;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}
