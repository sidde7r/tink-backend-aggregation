package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum BerlinGroupPaymentStatus {
    AcceptedCustomerProfile("ACCP", PaymentStatus.PENDING),
    AcceptedSettlementCompleted("ACSC", PaymentStatus.PAID),
    AcceptedSettlementInProcess("ACSP", PaymentStatus.PAID),
    AcceptedTechnicalValidation("ACTC", PaymentStatus.PAID),
    AcceptedWithChange("ACWC", PaymentStatus.CREATED),
    AcceptedWithoutPosting("ACWP", PaymentStatus.PENDING),
    Received("RCVD", PaymentStatus.PENDING),
    Pending("PNDG", PaymentStatus.PENDING),
    Rejected("RJCT", PaymentStatus.REJECTED),
    Canceled("CANC", PaymentStatus.CANCELLED),
    Undefined("Undefined", PaymentStatus.UNDEFINED);

    BerlinGroupPaymentStatus(String text, PaymentStatus paymentStatus) {
        this.text = text;
        this.paymentStatus = paymentStatus;
    }

    public static BerlinGroupPaymentStatus fromString(String text) {
        return Arrays.stream(BerlinGroupPaymentStatus.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(Undefined);
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
