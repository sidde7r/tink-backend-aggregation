package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum UnicreditPaymentStatus {
    RECEIVED("RCVD", PaymentStatus.PENDING),
    CANCELED("CANC", PaymentStatus.CANCELLED),
    PARTIALLY_ACCEPTED_TECHNICAL("PATC", PaymentStatus.PENDING),
    ACCEPTED_SETTLEMENT_IN_PROCESS("ACSP", PaymentStatus.SIGNED),
    ACCEPTED_WITH_CHANGE("ACWC", PaymentStatus.SIGNED),
    ACCEPTED_FUNDS_CHECKED("ACFC", PaymentStatus.SIGNED),
    PENDING("PDNG", PaymentStatus.PENDING),
    REJECTED("RJCT", PaymentStatus.REJECTED),
    ACCEPTED_SETTLEMENT_COMPLETED_ON_DEBTOR_ACCOUNT("ACSC", PaymentStatus.PAID),
    ACCEPTED_SETTLEMENT_COMPLETED_ON_CREDITOR_ACCOUNT("ACCC", PaymentStatus.PAID),
    ACCEPTED_CUSTOMER_PROFILE("ACCP", PaymentStatus.SIGNED),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC", PaymentStatus.SIGNED),
    ACCEPTED_WITHOUT_POSTING("ACWP", PaymentStatus.SIGNED),
    UNKNOWN("Unknown", PaymentStatus.UNDEFINED);

    private String statusText;
    private PaymentStatus paymentStatus;

    UnicreditPaymentStatus(String status, PaymentStatus paymentStatus) {
        this.statusText = status;
        this.paymentStatus = paymentStatus;
    }

    public static UnicreditPaymentStatus fromString(String text) {
        return Arrays.stream(UnicreditPaymentStatus.values())
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
