package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ErrorMessages;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum RaiffeisenPaymentStatus {
    ACCP("ACCP"),
    ACSC("ACSC"),
    ACSP("ACSP"),
    ACTC("ACTC"),
    ACWC("ACWC"),
    ACWP("ACWP"),
    RCVD("RCVD"),
    RJCT("RJCT"),
    PDNG("PDNG"),
    UNKNOWN("UNKNOWN");

    private String statusText;

    private static EnumMap<RaiffeisenPaymentStatus, PaymentStatus> icaPaymentStatusToTinkMapper =
            new EnumMap<>(RaiffeisenPaymentStatus.class);

    static {
        icaPaymentStatusToTinkMapper.put(ACCP, PaymentStatus.PENDING);
        icaPaymentStatusToTinkMapper.put(ACSC, PaymentStatus.PAID);
        icaPaymentStatusToTinkMapper.put(ACSP, PaymentStatus.PENDING);
        icaPaymentStatusToTinkMapper.put(ACTC, PaymentStatus.PENDING);
        icaPaymentStatusToTinkMapper.put(ACWC, PaymentStatus.PENDING);
        icaPaymentStatusToTinkMapper.put(ACWP, PaymentStatus.PENDING);
        icaPaymentStatusToTinkMapper.put(RCVD, PaymentStatus.CREATED);
        icaPaymentStatusToTinkMapper.put(RJCT, PaymentStatus.REJECTED);
        icaPaymentStatusToTinkMapper.put(PDNG, PaymentStatus.PENDING);
    }

    RaiffeisenPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return this.statusText;
    }

    public static RaiffeisenPaymentStatus fromString(String text) {
        return Arrays.stream(RaiffeisenPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(RaiffeisenPaymentStatus paymentStatus) {
        return Optional.ofNullable(icaPaymentStatusToTinkMapper.get(paymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                ErrorMessages.MAPPING, paymentStatus.toString())));
    }

    @Override
    public String toString() {
        return statusText;
    }
}
