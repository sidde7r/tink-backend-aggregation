package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentType;

public enum BnpParibasFortisPaymentType {
    SEPA("SEPA", PaymentType.SEPA),
    UNDEFINED("UNDEFINED", PaymentType.UNDEFINED);

    private final String text;
    private final PaymentType paymentType;

    BnpParibasFortisPaymentType(String text, PaymentType paymentType) {
        this.text = text;
        this.paymentType = paymentType;
    }

    @Override
    public String toString() {
        return text;
    }

    public PaymentType getTinkPaymentType() {
        return paymentType;
    }

    public static BnpParibasFortisPaymentType fromString(String text) {
        return Arrays.stream(BnpParibasFortisPaymentType.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNDEFINED);
    }
}
