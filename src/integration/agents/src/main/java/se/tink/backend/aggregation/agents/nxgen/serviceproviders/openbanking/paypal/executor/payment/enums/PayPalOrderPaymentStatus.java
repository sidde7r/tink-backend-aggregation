package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum PayPalOrderPaymentStatus {
    CREATED("CREATED"),
    APPROVED("APPROVED"),
    UNKNOWN("UNKNOWN");

    PayPalOrderPaymentStatus(String name) {
        this.statusName = name;
    }

    private String statusName;

    @Override
    public String toString() {
        return statusName;
    }

    private static EnumMap<PayPalOrderPaymentStatus, PaymentStatus>
            payPalPaymentStatusToTinkMapper = new EnumMap<>(PayPalOrderPaymentStatus.class);

    static {
        payPalPaymentStatusToTinkMapper.put(CREATED, PaymentStatus.PENDING);
        payPalPaymentStatusToTinkMapper.put(APPROVED, PaymentStatus.PAID);
    }

    public static PayPalOrderPaymentStatus fromString(String status) {
        return Arrays.stream(PayPalOrderPaymentStatus.values())
                .filter(s -> s.statusName.equalsIgnoreCase(status))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(
            PayPalOrderPaymentStatus payPalPaymentStatus) {
        return Optional.ofNullable(payPalPaymentStatusToTinkMapper.get(payPalPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "Cannot map PayPal payment status: %s to Tink payment status.",
                                                payPalPaymentStatus.toString())));
    }
}
