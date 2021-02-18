package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentAuthorizationFailedByUserException extends PaymentAuthorizationException {
    public static final String MESSAGE = "Authorisation of payment failed. Please try again.";

    public PaymentAuthorizationFailedByUserException(
            ErrorEntity errorEntity, InternalStatus internalStatus) {
        super(errorEntity, MESSAGE, internalStatus);
    }
}
