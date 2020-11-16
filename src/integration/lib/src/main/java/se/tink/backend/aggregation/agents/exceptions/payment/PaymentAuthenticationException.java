package se.tink.backend.aggregation.agents.exceptions.payment;

public class PaymentAuthenticationException extends PaymentException {
    public static final String DEFAULT_MESSAGE = "Payment authentication failed";

    public PaymentAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
