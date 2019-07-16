package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum PayPalPaymentStatus {
    CREATED("CREATED"),
    PROCESSED("PROCESSED"),
    UNKNOWN("UNKNOWN");

    PayPalPaymentStatus(String name) {
        this.statusName = name;
    }

    private String statusName;

    @Override
    public String toString() {
        return statusName;
    }

    private static EnumMap<PayPalPaymentStatus, PaymentStatus> payPalPaymentStatusToTinkMapper =
            new EnumMap<>(PayPalPaymentStatus.class);

    static {
        payPalPaymentStatusToTinkMapper.put(CREATED, PaymentStatus.PENDING);
        payPalPaymentStatusToTinkMapper.put(PROCESSED, PaymentStatus.PAID);
    }

    public static PayPalPaymentStatus fromString(String status) {
        return Arrays.stream(PayPalPaymentStatus.values())
                .filter(s -> s.statusName.equalsIgnoreCase(status))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(PayPalPaymentStatus payPalPaymentStatus) {
        return Optional.ofNullable(payPalPaymentStatusToTinkMapper.get(payPalPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "Cannot map PayPal payment status: %s to Tink payment status.",
                                                payPalPaymentStatus.toString())));
    }
}
