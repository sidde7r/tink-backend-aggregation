package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum BerlinGroupPaymentStatus {
    ACCEPTED_CUSTOMER_PROFILE("ACCP", PaymentStatus.PENDING),
    ACCEPTED_SETTLEMENT_COMPLETED("ACSC", PaymentStatus.PAID),
    ACCEPTED_SETTLEMENT_IN_PROCESS("ACSP", PaymentStatus.PAID),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC", PaymentStatus.PAID),
    ACCEPTED_WITH_CHANGE("ACWC", PaymentStatus.CREATED),
    ACCEPTED_WITHOUT_POSTING("ACWP", PaymentStatus.PENDING),
    RECEIVED("RCVD", PaymentStatus.PENDING),
    PENDING("PNDG", PaymentStatus.PENDING),
    REJECTED("RJCT", PaymentStatus.REJECTED),
    CANCELED("CANC", PaymentStatus.CANCELLED),
    UNDEFINED("Undefined", PaymentStatus.UNDEFINED);

    BerlinGroupPaymentStatus(String text, PaymentStatus paymentStatus) {
        this.text = text;
        this.paymentStatus = paymentStatus;
    }

    public static BerlinGroupPaymentStatus fromString(String text) {
        return Arrays.stream(BerlinGroupPaymentStatus.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNDEFINED);
    }

    private String text;
    private PaymentStatus paymentStatus;

    @Override
    public String toString() {
        return this.text;
    }

    public PaymentStatus getTinkPaymentStatus() {
        return paymentStatus;
    }
}
