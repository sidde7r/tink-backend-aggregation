package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum CbiGlobePaymentStatus {
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

    private static EnumMap<CbiGlobePaymentStatus, PaymentStatus> paymentStatusToTinkMapper =
            new EnumMap<>(CbiGlobePaymentStatus.class);

    static {
        paymentStatusToTinkMapper.put(ACCP, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACSC, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACSP, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACTC, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACWC, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACWP, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(RCVD, PaymentStatus.CREATED);
        paymentStatusToTinkMapper.put(RJCT, PaymentStatus.REJECTED);
        paymentStatusToTinkMapper.put(PDNG, PaymentStatus.PENDING);
    }

    CbiGlobePaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return this.statusText;
    }

    public static CbiGlobePaymentStatus fromString(String text) {
        return Arrays.stream(CbiGlobePaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(CbiGlobePaymentStatus paymentStatus) {
        return Optional.ofNullable(paymentStatusToTinkMapper.get(paymentStatus))
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
