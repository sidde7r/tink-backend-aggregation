package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.libraries.payment.enums.PaymentStatus;

@AllArgsConstructor
@Getter
public enum BankPaymentStatus {
    ACCEPTED_CUSTOMER_PROFILE("ACCP", PaymentStatus.SIGNED),
    ACCEPTED_SETTLEMENT_COMPLETED("ACSC", PaymentStatus.SIGNED),
    ACCEPTED_SETTLEMENT_IN_PROCESS("ACSP", PaymentStatus.SIGNED),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC", PaymentStatus.PENDING),
    ACCEPTED_WITH_CHANGE("ACWC", PaymentStatus.PENDING),
    ACCEPTED_WITHOUT_POSTING("ACWP", PaymentStatus.PENDING),
    PARTIALLY_ACCEPTED("PART", PaymentStatus.PENDING),
    RECEIVED("RCVD", PaymentStatus.CREATED),
    PENDING("PDNG", PaymentStatus.SIGNED),
    REJECTED("RJCT", PaymentStatus.REJECTED),
    UNKNOWN("UNKNOWN", PaymentStatus.UNDEFINED);

    private final String text;
    private final PaymentStatus paymentStatus;

    public static BankPaymentStatus fromString(String text) {
        return Arrays.stream(BankPaymentStatus.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
