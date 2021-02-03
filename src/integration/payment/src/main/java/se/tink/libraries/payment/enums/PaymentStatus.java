package se.tink.libraries.payment.enums;

public enum PaymentStatus {
    UNDEFINED,
    CREATED,
    PENDING,
    SIGNED,
    PAID,
    REJECTED,
    USER_APPROVAL_FAILED,
    CANCELLED,
    SETTLEMENT_COMPLETED
}
