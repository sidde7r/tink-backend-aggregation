package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentAuthorizationCancelledByUserException extends PaymentAuthorizationException {
    public static final String MESSAGE =
            "Authorisation of payment was cancelled. Please try again.";

    public PaymentAuthorizationCancelledByUserException(
            ErrorEntity errorEntity, InternalStatus internalStatus) {
        super(errorEntity, MESSAGE, internalStatus);
    }

    public PaymentAuthorizationCancelledByUserException() {
        super(MESSAGE, InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED);
    }

    public PaymentAuthorizationCancelledByUserException(String message, Throwable cause) {
        super(message, InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED, cause);
    }

    public PaymentAuthorizationCancelledByUserException(Throwable cause) {
        super(MESSAGE, InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED, cause);
    }
}
