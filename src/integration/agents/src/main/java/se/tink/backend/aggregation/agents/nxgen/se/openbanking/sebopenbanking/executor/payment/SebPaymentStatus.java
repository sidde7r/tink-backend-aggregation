package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum SebPaymentStatus {
    RCVD("RCVD"),
    ACTC("ACTC"),
    RJCT("RJCT"),
    ACSC("ACSC"),
    CANC("CANC"),
    UNKNOWN("Unknown");

    private String statusText;

    private static EnumMap<SebPaymentStatus, PaymentStatus>
            sebPaymentStatusToTinkPaymentStatusMapper =
                    new EnumMap<SebPaymentStatus, PaymentStatus>(SebPaymentStatus.class);

    static {
        sebPaymentStatusToTinkPaymentStatusMapper.put(RCVD, PaymentStatus.PENDING);
        sebPaymentStatusToTinkPaymentStatusMapper.put(RJCT, PaymentStatus.REJECTED);
        sebPaymentStatusToTinkPaymentStatusMapper.put(ACSC, PaymentStatus.PAID);
        sebPaymentStatusToTinkPaymentStatusMapper.put(ACTC, PaymentStatus.PAID);
        sebPaymentStatusToTinkPaymentStatusMapper.put(CANC, PaymentStatus.CANCELLED);
    }

    SebPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return statusText;
    }

    public static SebPaymentStatus fromString(String text) {
        return Arrays.stream(SebPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(SebPaymentStatus sebPaymentStatus) {
        return Optional.of(sebPaymentStatusToTinkPaymentStatusMapper.get(sebPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Seb payment status : "
                                                + sebPaymentStatus.toString()
                                                + " to Tink payment status."));
    }
}
