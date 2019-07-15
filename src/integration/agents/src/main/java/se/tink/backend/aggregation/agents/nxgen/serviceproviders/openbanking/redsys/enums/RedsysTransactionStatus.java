package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum RedsysTransactionStatus {
    ACCEPTED_SETTLEMENT_COMPLETED_ACCC("ACCC"),
    ACCEPTED_CUSTOMER_PROFILE("ACCP"),
    ACCEPTED_FUNDS_CHECKED("ACFC"),
    ACCEPTED_SETTLEMENT_COMPLETED_ACSC("ACSC"),
    ACCEPTED_SETTLEMENT_IN_PROCESS("ACSP"),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC"),
    ACCEPTED_WITH_CHANGE("ACWC"),
    ACCEPTED_WITHOUT_POSTING("ACWP"),
    RECEIVED("RCVD"),
    PARTIALLY_ACCEPTED_TECHNICAL_CORRECT("PATC"),
    PENDING("PDNG"),
    REJECTED("RJCT"),
    CANCELLED("CANC"),
    PARTIAL("PART"),
    UNKNOWN("????");

    private String statusText;

    RedsysTransactionStatus(String status) {
        this.statusText = status;
    }

    private static EnumMap<RedsysTransactionStatus, PaymentStatus> redsysPaymentStatusToTinkMapper =
            new EnumMap<>(RedsysTransactionStatus.class);

    static {
        redsysPaymentStatusToTinkMapper.put(ACCEPTED_SETTLEMENT_COMPLETED_ACCC, PaymentStatus.PAID);
        redsysPaymentStatusToTinkMapper.put(ACCEPTED_CUSTOMER_PROFILE, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(ACCEPTED_FUNDS_CHECKED, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(ACCEPTED_SETTLEMENT_COMPLETED_ACSC, PaymentStatus.PAID);
        redsysPaymentStatusToTinkMapper.put(ACCEPTED_SETTLEMENT_IN_PROCESS, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(ACCEPTED_TECHNICAL_VALIDATION, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(ACCEPTED_WITH_CHANGE, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(ACCEPTED_WITHOUT_POSTING, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(RECEIVED, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(
                PARTIALLY_ACCEPTED_TECHNICAL_CORRECT, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(PENDING, PaymentStatus.PENDING);
        redsysPaymentStatusToTinkMapper.put(REJECTED, PaymentStatus.REJECTED);
        redsysPaymentStatusToTinkMapper.put(CANCELLED, PaymentStatus.CANCELLED);
    }

    public static RedsysTransactionStatus fromString(String text) {
        return Arrays.stream(RedsysTransactionStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public PaymentStatus toTinkPaymentStatus() {
        return Optional.ofNullable(redsysPaymentStatusToTinkMapper.get(this))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Redsys payment status : "
                                                + this.statusText
                                                + " to Tink payment status."));
    }

    @Override
    public String toString() {
        return statusText;
    }
}
