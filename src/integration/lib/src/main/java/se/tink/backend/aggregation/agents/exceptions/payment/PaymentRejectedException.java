package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentRejectedException extends PaymentException {
    public static final String MESSAGE = "The payment was rejected by the bank.";
    public static final String TEMPORARILY_UNAVAILABLE_MESSAGE =
            "Bank service is unavailable to make payments right now. Please try again later.";
    public static final String FRAUDULENT_PAYMENT_MESSAGE =
            "The Payment Request is considered as fraudulent.";

    public PaymentRejectedException(String message) {
        super(message);
    }

    public PaymentRejectedException() {
        super(MESSAGE, InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION);
    }

    public PaymentRejectedException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public static PaymentRejectedException bankPaymentServiceUnavailable() {
        return new PaymentRejectedException(TEMPORARILY_UNAVAILABLE_MESSAGE);
    }

    public static PaymentRejectedException fraudulentPaymentException() {
        return new PaymentRejectedException(
                FRAUDULENT_PAYMENT_MESSAGE, InternalStatus.FRAUDULENT_PAYMENT);
    }
}
