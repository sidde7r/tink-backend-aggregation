package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.libraries.payment.enums.PaymentType;

@AllArgsConstructor
@Getter
public enum BnpParibasPaymentType {
    SEPA("SEPA", PaymentType.SEPA),
    UNDEFINED("UNDEFINED", PaymentType.UNDEFINED);

    private final String text;
    private final PaymentType paymentType;

    public static BnpParibasPaymentType fromString(String text) {
        return Arrays.stream(BnpParibasPaymentType.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNDEFINED);
    }
}
