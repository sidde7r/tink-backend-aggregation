package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.libraries.i18n.LocalizableKey;

public class BankIdException extends MultiFactorAuthenticationException {
    private final BankIdError error;

    public BankIdException(BankIdError error) {
        super(error);
        this.error = error;
    }

    public BankIdException(BankIdError error, LocalizableKey userMessage) {
        super(error, userMessage);
        this.error = error;
    }

    @Override
    public BankIdError getError() {
        return error;
    }
}
