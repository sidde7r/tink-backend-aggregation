package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.ErrorMessages;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum DkbPaymentStatus {
    ACCC("ACCC"),
    ACCP("ACCP"),
    ACSC("ACSC"),
    ACSP("ACSP"),
    ACTC("ACTC"),
    ACWC("ACWC"),
    ACWP("ACWP"),
    RCVD("RCVD"),
    PDNG("PDNG"),
    RJCT("RJCT"),
    CANC("CANC"),
    ACFC("ACFC"),
    PATC("PATC"),
    PART("PART"),
    UKNOWN("UKNOWN");

    private String statusText;

    private static EnumMap<DkbPaymentStatus, PaymentStatus> dkbPaymentStatusToTinkMapper =
            new EnumMap<>(DkbPaymentStatus.class);

    static {
        dkbPaymentStatusToTinkMapper.put(ACCC, PaymentStatus.PAID);
        dkbPaymentStatusToTinkMapper.put(ACCP, PaymentStatus.PENDING);
        dkbPaymentStatusToTinkMapper.put(ACSC, PaymentStatus.PAID);
        dkbPaymentStatusToTinkMapper.put(ACSP, PaymentStatus.PENDING);
        dkbPaymentStatusToTinkMapper.put(ACTC, PaymentStatus.PENDING);
        dkbPaymentStatusToTinkMapper.put(ACWC, PaymentStatus.PENDING);
        dkbPaymentStatusToTinkMapper.put(ACWP, PaymentStatus.PENDING);
        dkbPaymentStatusToTinkMapper.put(RCVD, PaymentStatus.CREATED);
        dkbPaymentStatusToTinkMapper.put(PDNG, PaymentStatus.PENDING);
        dkbPaymentStatusToTinkMapper.put(RJCT, PaymentStatus.REJECTED);
        dkbPaymentStatusToTinkMapper.put(CANC, PaymentStatus.CANCELLED);
        dkbPaymentStatusToTinkMapper.put(ACFC, PaymentStatus.PENDING);
        dkbPaymentStatusToTinkMapper.put(PATC, PaymentStatus.PENDING);
        dkbPaymentStatusToTinkMapper.put(PART, PaymentStatus.PENDING);
    }

    DkbPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return this.statusText;
    }

    public static DkbPaymentStatus fromString(String text) {
        return Arrays.stream(DkbPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(DkbPaymentStatus dkbPaymentStatus) {
        return Optional.ofNullable(dkbPaymentStatusToTinkMapper.get(dkbPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                ErrorMessages
                                                        .MAPING_DKB_PAYMENT_TO_TINK_PAYMENT_ERROR,
                                                dkbPaymentStatus.toString())));
    }

    @Override
    public String toString() {
        return statusText;
    }
}
