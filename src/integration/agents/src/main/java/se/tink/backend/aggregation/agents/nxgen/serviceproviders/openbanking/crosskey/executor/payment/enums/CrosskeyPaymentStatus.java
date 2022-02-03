package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.enums;

import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum CrosskeyPaymentStatus {
    AWAITING_AUTHORISATION("AwaitingAuthorisation"),
    AUTHORISED("Authorised"),
    ACCEPTED_SETTLEMENT_COMPLETED("AcceptedSettlementCompleted"),
    ACCEPTED_SETTLEMENT_IN_PROCESS("AcceptedSettlementInProcess"),
    UNKNOWN("Unknown"),
    CONSUMED("Consumed"),
    REJECTED("Rejected");

    private static EnumMap<CrosskeyPaymentStatus, PaymentStatus> crosskeyPaymentStatusToTinkMapper =
            new EnumMap<>(CrosskeyPaymentStatus.class);
    private static EnumMap<PaymentStatus, CrosskeyPaymentStatus> tinkPaymentStatusToCrosskeyMapper =
            new EnumMap<>(PaymentStatus.class);

    static {
        crosskeyPaymentStatusToTinkMapper.put(AWAITING_AUTHORISATION, PaymentStatus.PENDING);
        crosskeyPaymentStatusToTinkMapper.put(AUTHORISED, PaymentStatus.SIGNED);
        crosskeyPaymentStatusToTinkMapper.put(ACCEPTED_SETTLEMENT_COMPLETED, PaymentStatus.PAID);
        crosskeyPaymentStatusToTinkMapper.put(ACCEPTED_SETTLEMENT_IN_PROCESS, PaymentStatus.PAID);
        crosskeyPaymentStatusToTinkMapper.put(CONSUMED, PaymentStatus.PAID);
        crosskeyPaymentStatusToTinkMapper.put(UNKNOWN, PaymentStatus.UNDEFINED);
        crosskeyPaymentStatusToTinkMapper.put(REJECTED, PaymentStatus.REJECTED);

        tinkPaymentStatusToCrosskeyMapper.put(PaymentStatus.PENDING, AWAITING_AUTHORISATION);
        tinkPaymentStatusToCrosskeyMapper.put(PaymentStatus.SIGNED, AUTHORISED);
        tinkPaymentStatusToCrosskeyMapper.put(PaymentStatus.CREATED, AWAITING_AUTHORISATION);
    }

    private String statusText;

    CrosskeyPaymentStatus(String statusText) {
        this.statusText = statusText;
    }

    public static PaymentStatus mapToTinkPaymentStatus(
            CrosskeyPaymentStatus crosskeyPaymentStatus) {
        return Optional.ofNullable(crosskeyPaymentStatusToTinkMapper.get(crosskeyPaymentStatus))
                .orElseThrow(
                        () ->
                                createException(
                                        CrosskeyBaseConstants.ExceptionMessagePatterns
                                                .CANNOT_MAP_CROSSKEY_PAYMENT_STATUS,
                                        crosskeyPaymentStatus.toString()));
    }

    public static CrosskeyPaymentStatus mapToCrosskeyPaymentStatus(PaymentStatus paymentStatus) {
        return Optional.ofNullable(tinkPaymentStatusToCrosskeyMapper.get(paymentStatus))
                .orElseThrow(
                        () ->
                                createException(
                                        CrosskeyBaseConstants.ExceptionMessagePatterns
                                                .CANNOT_MAP_TINK_PAYMENT_STATUS,
                                        paymentStatus.name()));
    }

    private static IllegalArgumentException createException(String format, String param) {
        return new IllegalArgumentException(String.format(format, param));
    }

    public String getText() {
        return this.statusText;
    }

    @Override
    public String toString() {
        return statusText;
    }

    public static CrosskeyPaymentStatus fromString(String text) throws IllegalArgumentException {
        for (CrosskeyPaymentStatus status : CrosskeyPaymentStatus.values()) {
            if (status.statusText.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw createException(
                CrosskeyBaseConstants.ExceptionMessagePatterns.CANNOT_MAP_CROSSKEY_PAYMENT_STATUS,
                text);
    }
}
