package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentRejectedException extends PaymentException {
    public static final String MESSAGE = "The payment was rejected by the bank.";
    public static final String SIMILAR_PAYMENT_ERROR_MESSAGE =
            "A similar payment is already in upcoming events.";
    public static final String TEMPORARILY_UNAVAILABLE_MESSAGE =
            "Bank service is unavailable to make payments right now. Please try again later.";

    public PaymentRejectedException(String message) {
        super(message);
    }

    public PaymentRejectedException() {
        this(MESSAGE);
    }

    public static PaymentRejectedException similarPaymentException() {
        return new PaymentRejectedException(SIMILAR_PAYMENT_ERROR_MESSAGE);
    }

    public static PaymentRejectedException bankPaymentServiceUnavailable() {
        return new PaymentRejectedException(TEMPORARILY_UNAVAILABLE_MESSAGE);
    }
}
