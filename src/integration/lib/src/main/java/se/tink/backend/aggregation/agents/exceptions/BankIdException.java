package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class BankIdException extends MultiFactorAuthenticationException {

    public BankIdException(BankIdError error) {
        super(error);
    }

    public BankIdException(BankIdError error, Throwable cause) {
        super(error, cause);
    }

    public BankIdException(BankIdError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public BankIdException(BankIdError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public BankIdException(AgentError error, String internalMessage) {
        super(error, internalMessage);
    }

    @Override
    public BankIdError getError() {
        return getError(BankIdError.class);
    }
}
