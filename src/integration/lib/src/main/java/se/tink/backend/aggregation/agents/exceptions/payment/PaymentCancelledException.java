package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentCancelledException extends PaymentException {

    public static final String MESSAGE = "The payment was cancelled by the user.";

    public PaymentCancelledException(String message) {
        super(message);
    }

    public PaymentCancelledException() {
        this(MESSAGE);
    }
}
