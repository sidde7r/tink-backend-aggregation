package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.libraries.payment.enums.PaymentStatus;

@AllArgsConstructor
@Getter
public enum N26PaymentStatus {
    INVALID_STATUS("INVALID_STATUS", PaymentStatus.REJECTED),
    PENDING("PENDING", PaymentStatus.PENDING),
    PROCESSING("PROCESSING", PaymentStatus.PENDING),
    SUCCESS("SUCCESS", PaymentStatus.SIGNED),
    PENDING_EXTERNAL_AUTHORIZATION("PENDING_EXTERNAL_AUTHORIZATION", PaymentStatus.PENDING),
    FAILURE_CANCELED("FAILURE_CANCELED", PaymentStatus.REJECTED),
    FAILURE_INSUFFICIENT_FUNDS("FAILURE_INSUFFICIENT_FUNDS", PaymentStatus.REJECTED),
    FAILURE_INVALID_CURRENCY("FAILURE_INVALID_CURRENCY", PaymentStatus.REJECTED),
    FAILURE_PERMISSION_DENIED("FAILURE_PERMISSION_DENIED", PaymentStatus.REJECTED),
    FAILURE_QUOTE_EXPIRED("FAILURE_QUOTE_EXPIRED", PaymentStatus.REJECTED),
    FAILURE_INVALID_AMOUNT("FAILURE_INVALID_AMOUNT", PaymentStatus.REJECTED),
    FAILURE_INVALID_QUOTE("FAILURE_INVALID_QUOTE", PaymentStatus.REJECTED),
    FAILURE_EXPIRED("FAILURE_EXPIRED", PaymentStatus.REJECTED),
    FAILURE_GENERIC("FAILURE_GENERIC", PaymentStatus.REJECTED),
    SENT("SENT", PaymentStatus.PENDING),
    INITIATED("INITIATED", PaymentStatus.CREATED),
    UNKNOWN("UNKNOWN", PaymentStatus.UNDEFINED);

    private final String text;
    private final PaymentStatus paymentStatus;

    public static N26PaymentStatus fromString(String text) {
        return Arrays.stream(N26PaymentStatus.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
