package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class InsufficientFundsException extends DebtorValidationException {
    public static final String DEFAULT_MESSAGE =
            "Could not execute payment due to insufficient funds.";

    public InsufficientFundsException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }
}
