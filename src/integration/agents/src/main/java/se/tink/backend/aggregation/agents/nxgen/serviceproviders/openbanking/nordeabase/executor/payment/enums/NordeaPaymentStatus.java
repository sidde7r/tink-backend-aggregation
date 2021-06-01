package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

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
    CANCELLED("Cancelled"),
    UNKNOWN("Unknown");

    private static EnumMap<NordeaPaymentStatus, PaymentStatus> nordeaPaymentStatusToTinkMapper =
            new EnumMap<>(NordeaPaymentStatus.class);

    static {
        nordeaPaymentStatusToTinkMapper.put(PAID, PaymentStatus.PAID);
        nordeaPaymentStatusToTinkMapper.put(REJECTED, PaymentStatus.REJECTED);
        nordeaPaymentStatusToTinkMapper.put(PENDING_USER_APPROVAL, PaymentStatus.PENDING);
        nordeaPaymentStatusToTinkMapper.put(PENDING_CONFIRMATION, PaymentStatus.PENDING);
        nordeaPaymentStatusToTinkMapper.put(CONFIRMED, PaymentStatus.SIGNED);
        nordeaPaymentStatusToTinkMapper.put(
                USER_APPROVAL_FAILED, PaymentStatus.USER_APPROVAL_FAILED);
        nordeaPaymentStatusToTinkMapper.put(
                USER_APPROVAL_TIMEOUT, PaymentStatus.USER_APPROVAL_FAILED);
        nordeaPaymentStatusToTinkMapper.put(USER_APPROVAL_CANCELLED, PaymentStatus.CANCELLED);
        nordeaPaymentStatusToTinkMapper.put(CANCELLED, PaymentStatus.CANCELLED);
        nordeaPaymentStatusToTinkMapper.put(ON_HOLD, PaymentStatus.PENDING);
        nordeaPaymentStatusToTinkMapper.put(UNKNOWN, PaymentStatus.UNDEFINED);
    }

    private String statusText;

    NordeaPaymentStatus(String status) {
        this.statusText = status;
    }

    public static NordeaPaymentStatus fromString(String text) {
        return Arrays.stream(NordeaPaymentStatus.values())
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

    public String getText() {
        return this.statusText;
    }

    @Override
    public String toString() {
        return statusText;
    }
}
