package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentStatus {
    NONE("NONE"),
    RECEIVED("RECEIVED"),
    LOSS("LOSS"),
    UNKNOWN("Unknown");

    private String status;

    public static PaymentStatus fromString(String text) {
        return Arrays.stream(PaymentStatus.values())
                .filter(s -> s.status.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return status;
    }
}
