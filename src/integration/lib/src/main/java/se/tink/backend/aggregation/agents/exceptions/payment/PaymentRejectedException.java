package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentRejectedException extends PaymentException {
    public static final String MESSAGE = "The payment was rejected by the bank.";

    public PaymentRejectedException(String message) {
        super(message);
    }

    public PaymentRejectedException() {
        this(MESSAGE);
    }
}
