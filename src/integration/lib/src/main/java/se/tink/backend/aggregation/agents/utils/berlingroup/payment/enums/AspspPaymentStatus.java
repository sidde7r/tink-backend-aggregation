package se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.libraries.payment.enums.PaymentStatus;

@AllArgsConstructor
@Getter
public enum AspspPaymentStatus {
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

    public static AspspPaymentStatus fromString(String text) {
        return Arrays.stream(AspspPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return statusText;
    }
}
