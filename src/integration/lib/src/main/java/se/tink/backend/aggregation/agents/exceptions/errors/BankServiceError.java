package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.libraries.i18n.LocalizableKey;

public enum BankServiceError implements AgentRuntimeError {
    NO_BANK_SERVICE(new LocalizableKey("The bank service is offline, please try again later.")),
    BANK_SIDE_FAILURE(new LocalizableKey("The bank service has temporarily failed, please try again later."));


    private final LocalizableKey userMessage;

    BankServiceError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public BankServiceException exception() {
        return new BankServiceException(this);
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public BankServiceException exception(LocalizableKey userMessage) {
        return new BankServiceException(this, userMessage);
    }
}
