package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentRuntimeExceptionImpl;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.libraries.i18n.LocalizableKey;

public class BankServiceException extends AgentRuntimeExceptionImpl {

    public BankServiceException(BankServiceError error) {
        super(error);
    }

    public BankServiceException(BankServiceError error, Throwable cause) {
        super(error, cause);
    }

    public BankServiceException(BankServiceError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public BankServiceException(
            BankServiceError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public BankServiceException(BankServiceError error, String internalMessage) {
        super(error, internalMessage);
    }

    @Override
    public BankServiceError getError() {
        return getError(BankServiceError.class);
    }
}
