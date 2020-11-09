package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentAuthorizationException extends PaymentException {
    public static final String DEFAULT_MESSAGE = "Payment was not authorised. Please try again.";
    protected ErrorEntity errorEntity;

    public PaymentAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentAuthorizationException(String message) {
        super(message);
    }

    public PaymentAuthorizationException() {
        super(DEFAULT_MESSAGE);
    }

    public PaymentAuthorizationException(ErrorEntity errorEntity, String message) {
        super(message);
        this.errorEntity = errorEntity;
    }

    public PaymentAuthorizationException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public PaymentAuthorizationException(
            String message, InternalStatus internalStatus, AuthenticationException cause) {
        super(message, internalStatus, cause);
    }
}
