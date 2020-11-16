package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;

public class PaymentAuthorizationFailedByUserException extends PaymentAuthorizationException {
    public static final String MESSAGE = "Authorisation of payment failed. Please try again.";

    public PaymentAuthorizationFailedByUserException(ErrorEntity errorEntity) {
        super(errorEntity, MESSAGE);
    }
}
