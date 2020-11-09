package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;

public class PaymentAuthorizationTimeOutException extends PaymentAuthorizationException {
    public static final String MESSAGE = "Authorisation of payment timed out. Please try again.";

    public PaymentAuthorizationTimeOutException(String message) {
        super(message);
    }

    public PaymentAuthorizationTimeOutException() {
        this(MESSAGE);
    }

    public PaymentAuthorizationTimeOutException(ErrorEntity errorEntity) {
        super(errorEntity, MESSAGE);
    }
}
