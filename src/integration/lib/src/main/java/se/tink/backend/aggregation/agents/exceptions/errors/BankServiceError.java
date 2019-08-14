package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.libraries.i18n.LocalizableKey;

public enum BankServiceError implements AgentRuntimeError {
    NO_BANK_SERVICE(new LocalizableKey("The bank service is offline; please try again later.")),
    BANK_SIDE_FAILURE(
            new LocalizableKey("The bank service has temporarily failed; please try again later.")),
    ACCESS_EXCEEDED(
            new LocalizableKey(
                    "The maximum allowed number of requests to the bank service has been exceeded; please try again later.")),
    // This should probably be a (checked) authorization error, but it's not supported by
    // OAuth2Authenticator
    CONSENT_REVOKED(
            new LocalizableKey(
                    "The consent given to us to access your data has been revoked by the bank. ")),
    ;

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
