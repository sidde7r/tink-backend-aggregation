package se.tink.backend.aggregation.agents.exceptions.errors.no;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentExceptionImpl;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.libraries.i18n.LocalizableKey;

public enum BankIdErrorNO implements AgentError {
    CANCELLED(new LocalizableKey("You cancelled the BankID process. Please try again.")),
    TIMEOUT(new LocalizableKey("The mobile BankID session timed out. Please try again.")),
    UNKNOWN(new LocalizableKey("Something went wrong with the BankId authentication."));

    private final LocalizableKey userMessage;

    BankIdErrorNO(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public BankIdException exception() {
        return BankIdError.valueOf(this.name()).exception(this.userMessage);
    }

    @Override
    public BankIdException exception(String internalMessage) {
        return new BankIdException(this, internalMessage);
    }

    @Override
    public AgentExceptionImpl exception(Throwable cause) {
        return BankIdError.valueOf(this.name()).exception(this.userMessage, cause);
    }

    @Override
    public BankIdException exception(LocalizableKey userMessage) {
        return BankIdError.valueOf(this.name()).exception(userMessage);
    }

    @Override
    public AgentExceptionImpl exception(LocalizableKey userMessage, Throwable cause) {
        return BankIdError.valueOf(this.name()).exception(userMessage, cause);
    }
}
