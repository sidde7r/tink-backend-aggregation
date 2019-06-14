package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum SwedbankPaymentStatus {
    REJECTED("RJCT"),
    CANCELED("CANC"),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC"),
    PARTIALLY_ACCEPTED_TECHNICAL_VALIDATION("PATC"),
    ACCEPTER_CUSTOMER_PROFILE("ACCP"),
    ACCEPTER_SETTLEMENT_COMPLETED_DEBTOR("ACSC"),
    ACCEPTER_SETTLEMENT_COMPLETED_CREDITOR("ACCC"),
    UNKNOWN("Unknown");

    private String text;

    private static final EnumMap<SwedbankPaymentStatus, PaymentStatus>
            swedbankPaymentStatusPaymentStatusMapper = new EnumMap<>(SwedbankPaymentStatus.class);

    static {
        swedbankPaymentStatusPaymentStatusMapper.put(
                ACCEPTER_CUSTOMER_PROFILE, PaymentStatus.PENDING);
        swedbankPaymentStatusPaymentStatusMapper.put(
                ACCEPTED_TECHNICAL_VALIDATION, PaymentStatus.PENDING);
        swedbankPaymentStatusPaymentStatusMapper.put(
                PARTIALLY_ACCEPTED_TECHNICAL_VALIDATION, PaymentStatus.PENDING);
        swedbankPaymentStatusPaymentStatusMapper.put(
                ACCEPTER_SETTLEMENT_COMPLETED_DEBTOR, PaymentStatus.PAID);
        swedbankPaymentStatusPaymentStatusMapper.put(
                ACCEPTER_SETTLEMENT_COMPLETED_CREDITOR, PaymentStatus.PAID);
        swedbankPaymentStatusPaymentStatusMapper.put(CANCELED, PaymentStatus.CANCELLED);
        swedbankPaymentStatusPaymentStatusMapper.put(REJECTED, PaymentStatus.REJECTED);
        swedbankPaymentStatusPaymentStatusMapper.put(UNKNOWN, PaymentStatus.UNDEFINED);
    }

    SwedbankPaymentStatus(String text) {
        this.text = text;
    }

    public static SwedbankPaymentStatus fromString(String text) {
        return Arrays.stream(SwedbankPaymentStatus.values())
                .filter(s -> s.text.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(
            SwedbankPaymentStatus swedbankPaymentStatus) {
        return Optional.ofNullable(
                        swedbankPaymentStatusPaymentStatusMapper.get(swedbankPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Swedbank payment status: "
                                                + swedbankPaymentStatus.toString()
                                                + " to Tink payment status."));
    }

    @Override
    public String toString() {
        return text;
    }
}
