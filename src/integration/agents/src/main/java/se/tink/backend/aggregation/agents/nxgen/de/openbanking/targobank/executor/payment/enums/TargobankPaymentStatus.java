package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.ErrorMessages;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum TargobankPaymentStatus {
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

    private static EnumMap<TargobankPaymentStatus, PaymentStatus> icaPaymentStatusToTinkMapper =
            new EnumMap<>(TargobankPaymentStatus.class);

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

    TargobankPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return this.statusText;
    }

    public static TargobankPaymentStatus fromString(String text) {
        return Arrays.stream(TargobankPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(TargobankPaymentStatus paymentStatus) {
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
