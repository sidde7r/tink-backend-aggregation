package se.tink.backend.product.execution.unit.agents.exceptions;

import se.tink.backend.product.execution.unit.agents.exceptions.errors.BankServiceError;
import se.tink.libraries.i18n.LocalizableKey;

public class BankServiceException extends AuthenticationException {
    private BankServiceError error;

    public BankServiceException(BankServiceError error) {
        super(error);
        this.error = error;
    }

    public BankServiceException(BankServiceError error, LocalizableKey userMessage) {
        super(error, userMessage);
        this.error = error;
    }

    @Override
    public BankServiceError getError() {
        return error;
    }
}
