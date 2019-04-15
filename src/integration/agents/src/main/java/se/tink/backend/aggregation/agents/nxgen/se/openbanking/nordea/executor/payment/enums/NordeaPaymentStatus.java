package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.enums;

import se.tink.libraries.payment.enums.PaymentStatus;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;

public enum NordeaPaymentStatus {
    PENDING_CONFIRMATION("PendingConfirmation"),
    PENDING_USER_APPROVAL("PendingUserApproval"),
    ON_HOLD("OnHold"),
    CONFIRMED("Confirmed"),
    REJECTED("Rejected"),
    PAID("Paid"),
    INSUFFICIENT_FUNDS("InsufficientFunds"),
    LIMIT_EXCEEDED("LimitExceeded"),
    USER_APPROVAL_FAILED("UserApprovalFailed"),
    USER_APPROVAL_TIMEOUT("UserApprovalTimeout"),
    USER_APPROVAL_CANCELLED("UserApprovalCancelled"),
    UNKNOWN("Unknown");

    private String statusText;

    private static EnumMap<NordeaPaymentStatus, PaymentStatus> nordeaPaymentStatusToTinkMapper =
            new EnumMap<>(NordeaPaymentStatus.class);

    static {
        nordeaPaymentStatusToTinkMapper.put(PAID, PaymentStatus.PAID);
        nordeaPaymentStatusToTinkMapper.put(REJECTED, PaymentStatus.REJECTED);
        nordeaPaymentStatusToTinkMapper.put(PENDING_USER_APPROVAL, PaymentStatus.PENDING);
        nordeaPaymentStatusToTinkMapper.put(PENDING_CONFIRMATION, PaymentStatus.PENDING);
        nordeaPaymentStatusToTinkMapper.put(CONFIRMED, PaymentStatus.SIGNED);
        nordeaPaymentStatusToTinkMapper.put(UNKNOWN, PaymentStatus.UNDEFINED);
    }

    NordeaPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return this.statusText;
    }

    public static NordeaPaymentStatus fromString(String text) {
        return
                Arrays.stream(NordeaPaymentStatus.values())
                        .filter(s -> s.statusText.equalsIgnoreCase(text))
                        .findFirst()
                        .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(NordeaPaymentStatus nordeaPaymentStatus) {
        return Optional.ofNullable(nordeaPaymentStatusToTinkMapper.get(nordeaPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Nordea payment status : "
                                                + nordeaPaymentStatus.toString()
                                                + " to Tink payment status."));
    }

    @Override
    public String toString() {
        return statusText;
    }
}
