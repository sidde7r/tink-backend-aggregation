package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentRejectedException extends PaymentException {
    public static final String MESSAGE = "The payment was rejected by the bank.";
    public static final String TEMPORARILY_UNAVAILABLE_MESSAGE =
            "Bank service is unavailable to make payments right now. Please try again later.";

    public PaymentRejectedException(String message) {
        super(message);
    }

    public PaymentRejectedException() {
        this(MESSAGE);
    }

    public static PaymentRejectedException bankPaymentServiceUnavailable() {
        return new PaymentRejectedException(TEMPORARILY_UNAVAILABLE_MESSAGE);
    }

    public PaymentRejectedException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }
}
