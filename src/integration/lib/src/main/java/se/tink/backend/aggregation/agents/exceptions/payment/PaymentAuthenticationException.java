package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentAuthenticationException extends PaymentException {
    public PaymentAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
