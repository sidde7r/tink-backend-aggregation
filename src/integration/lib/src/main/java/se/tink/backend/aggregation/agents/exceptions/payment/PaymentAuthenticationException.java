package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentAuthenticationException extends PaymentException {
    public static final String DEFAULT_MESSAGE = "Payment authentication failed.";

    public PaymentAuthenticationException(InternalStatus internalStatus) {
        super(DEFAULT_MESSAGE, internalStatus);
    }

    public PaymentAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
