package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentAuthorizationException extends PaymentException {
    public PaymentAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
