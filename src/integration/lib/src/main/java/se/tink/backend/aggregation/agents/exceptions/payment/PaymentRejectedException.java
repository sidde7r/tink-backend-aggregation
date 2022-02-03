package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentRejectedException extends PaymentException {
    public static final String MESSAGE = "The payment was rejected by the bank.";
    private static final String TEMPORARILY_UNAVAILABLE_MESSAGE =
            "Bank service is unavailable to make payments right now. Please try again later.";
    private static final String FRAUDULENT_PAYMENT_MESSAGE =
            "The Payment Request is considered as fraudulent.";
    private static final String TOO_MANY_TRANSACTIONS =
            "The number of transactions exceeds the acceptance limit.";
    private static final String REJECTED_DUE_TO_REGULATORY_REASONS =
            "Rejected due to regulatory reasons.";

    public PaymentRejectedException(String message) {
        super(message);
    }

    public PaymentRejectedException() {
        super(MESSAGE, InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION);
    }

    public PaymentRejectedException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public PaymentRejectedException(Throwable throwable) {
        super(MESSAGE, InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION, throwable);
    }

    public PaymentRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PaymentRejectedException bankPaymentServiceUnavailable() {
        return new PaymentRejectedException(TEMPORARILY_UNAVAILABLE_MESSAGE);
    }

    public static PaymentRejectedException fraudulentPaymentException() {
        return new PaymentRejectedException(
                FRAUDULENT_PAYMENT_MESSAGE, InternalStatus.FRAUDULENT_PAYMENT);
    }

    public static PaymentRejectedException tooManyTransactions() {
        return new PaymentRejectedException(
                TOO_MANY_TRANSACTIONS, InternalStatus.TRANSFER_LIMIT_REACHED);
    }

    public static PaymentRejectedException rejectedDueToRegulatoryReasons() {
        return new PaymentRejectedException(
                REJECTED_DUE_TO_REGULATORY_REASONS, InternalStatus.PAYMENT_REGULATORY_REJECTED);
    }
}
