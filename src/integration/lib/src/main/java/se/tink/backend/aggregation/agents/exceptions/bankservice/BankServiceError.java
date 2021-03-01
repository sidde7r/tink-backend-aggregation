package se.tink.backend.aggregation.agents.exceptions.bankservice;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public enum BankServiceError implements AgentError {
    NO_BANK_SERVICE(new LocalizableKey("The bank service is offline; please try again later.")),
    BANK_SIDE_FAILURE(
            new LocalizableKey("The bank service has temporarily failed; please try again later.")),
    ACCESS_EXCEEDED(
            new LocalizableKey(
                    "The maximum allowed number of requests to the bank service has been exceeded; please try again later.")),
    MULTIPLE_LOGIN(
            new LocalizableKey(
                    "You were automatically logged out because you logged in to another channel; Logout is done for your security.")),
    SESSION_TERMINATED(new LocalizableKey("Your session has been terminated by the bank.")),
    DEFAULT_MESSAGE(new LocalizableKey("Something went wrong during the process."));

    private final LocalizableKey userMessage;

    BankServiceError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    public BankServiceException exception(String internalMessage) {
        return new BankServiceException(this, internalMessage);
    }

    @Override
    public BankServiceException exception() {
        return new BankServiceException(this);
    }

    @Override
    public BankServiceException exception(Throwable cause) {
        return new BankServiceException(this, cause);
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public BankServiceException exception(LocalizableKey userMessage) {
        return new BankServiceException(this, userMessage);
    }

    @Override
    public BankServiceException exception(LocalizableKey userMessage, Throwable cause) {
        return new BankServiceException(this, userMessage, cause);
    }
}
