package se.tink.backend.aggregation.agents.exceptions.bankidno;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public enum BankIdNOError implements AgentError {
    INITIALIZATION_ERROR(new LocalizableKey("Could not initialize BankID authentication.")),
    UNKNOWN_BANK_ID_ERROR(new LocalizableKey("BankID authentication error."));

    private final LocalizableKey userMessage;

    BankIdNOError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public BankIdNOException exception() {
        return new BankIdNOException(this);
    }

    @Override
    public BankIdNOException exception(String internalMessage) {
        return new BankIdNOException(this, internalMessage);
    }

    @Override
    public BankIdNOException exception(Throwable cause) {
        return new BankIdNOException(this, cause);
    }

    @Override
    public BankIdNOException exception(LocalizableKey userMessage) {
        return new BankIdNOException(this, userMessage);
    }

    @Override
    public BankIdNOException exception(LocalizableKey userMessage, Throwable cause) {
        return new BankIdNOException(this, userMessage, cause);
    }
}
