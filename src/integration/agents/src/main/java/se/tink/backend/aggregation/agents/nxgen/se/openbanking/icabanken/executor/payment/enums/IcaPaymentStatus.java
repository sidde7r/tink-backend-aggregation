package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ErrorMessages;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum IcaPaymentStatus {
    ACCP("ACCP"),
    ACSC("ACSC"),
    ACSP("ACSP"),
    ACTC("ACTC"),
    ACWC("ACWC"),
    ACWP("ACWP"),
    RCVD("RCVD"),
    RJCT("RJCT"),
    PDNG("PDNG"),
    CANC("CANC"),
    UNKNOWN("UNKNOWN");

    private String statusText;

    private static EnumMap<IcaPaymentStatus, PaymentStatus> icaPaymentStatusToTinkMapper =
            new EnumMap<>(IcaPaymentStatus.class);

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
        icaPaymentStatusToTinkMapper.put(CANC, PaymentStatus.CANCELLED);
        icaPaymentStatusToTinkMapper.put(UNKNOWN, PaymentStatus.UNDEFINED);
    }

    IcaPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return this.statusText;
    }

    public static IcaPaymentStatus fromString(String text) {
        return Arrays.stream(IcaPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(IcaPaymentStatus icaPaymentStatus) {
        return Optional.ofNullable(icaPaymentStatusToTinkMapper.get(icaPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                ErrorMessages.MAPPING,
                                                icaPaymentStatus.toString())));
    }

    @Override
    public String toString() {
        return statusText;
    }
}
