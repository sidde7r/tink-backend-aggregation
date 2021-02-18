package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentAuthorizationTimeOutException extends PaymentAuthorizationException {
    public static final String MESSAGE = "Authorisation of payment timed out. Please try again.";

    public PaymentAuthorizationTimeOutException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public PaymentAuthorizationTimeOutException(InternalStatus internalStatus) {
        this(MESSAGE, internalStatus);
    }

    public PaymentAuthorizationTimeOutException(
            ErrorEntity errorEntity, InternalStatus internalStatus) {
        super(errorEntity, MESSAGE, internalStatus);
    }
}
