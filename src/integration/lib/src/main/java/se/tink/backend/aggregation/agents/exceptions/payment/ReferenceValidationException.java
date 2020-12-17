package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class ReferenceValidationException extends PaymentValidationException {
    public static final String DEFAULT_MESSAGE =
            "The reference you provided for the payment is not valid";

    public ReferenceValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public ReferenceValidationException(String message) {
        super(message);
    }

    public ReferenceValidationException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public ReferenceValidationException(
            String message, String path, InternalStatus internalStatus, Throwable cause) {
        super(message, path, internalStatus, cause);
    }
}
