package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum SparebankPaymentStatus {
    ACCEPTED_CUSTOMER_PROFILE("ACCP", PaymentStatus.PAID),
    ACCEPTED_SETTLEMENT_CREDITOR_COMPLETED("ACCC", PaymentStatus.PAID),
    ACCEPTED_SETTLEMENT_DEBTOR_COMPLETED("ACSC", PaymentStatus.PAID),
    ACCEPTED_SETTLEMENT_IN_PROCESS("ACSP", PaymentStatus.PENDING),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC", PaymentStatus.PENDING),
    ACCEPTED_WITH_CHANGE("ACWC", PaymentStatus.PENDING),
    ACCEPTED_WITHOUT_POSTING("ACWP", PaymentStatus.PENDING),
    RECEIVED("RCVD", PaymentStatus.CREATED),
    PENDING("PDNG", PaymentStatus.SIGNED),
    REJECTED("RJCT", PaymentStatus.REJECTED),
    CANCELED("CANC", PaymentStatus.CANCELLED),
    PARTIALLY_ACCEPTED("PART", PaymentStatus.PENDING),
    UNDEFINED("Undefined", PaymentStatus.UNDEFINED);

    private String name;
    private PaymentStatus paymentStatus;

    SparebankPaymentStatus(String name, PaymentStatus paymentStatus) {
        this.name = name;
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return name;
    }

    public PaymentStatus getTinkPaymentStatus() {
        return paymentStatus;
    }

    public static SparebankPaymentStatus fromString(String text) {
        return Arrays.stream(SparebankPaymentStatus.values())
                .filter(s -> s.name.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNDEFINED);
    }
}
