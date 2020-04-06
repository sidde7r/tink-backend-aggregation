package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentRejectedException extends PaymentException {
    public static final String MESSAGE = "The payment was rejected by the bank.";
    public static final String SIMILAR_PAYMENT_ERROR_MESSAGE =
            "A similar payment is already in upcoming events.";

    public PaymentRejectedException(String message) {
        super(message);
    }

    public PaymentRejectedException() {
        this(MESSAGE);
    }

    public static PaymentRejectedException similarPaymentException() {
        return new PaymentRejectedException(SIMILAR_PAYMENT_ERROR_MESSAGE);
    }
}
