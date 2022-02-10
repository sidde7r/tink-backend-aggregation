package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public enum BankIdError implements AgentError {
    CANCELLED(new LocalizableKey("You cancelled the BankID process. Please try again.")),
    /** Indicates that the BankID app was opened but the user did not sign within the time window */
    TIMEOUT(
            new LocalizableKey(
                    "Authenticating with Mobile BankID timed out. Have you opened the app?")),
    /** Indicates that the BankID app was never opened */
    NO_CLIENT(new LocalizableKey("No response from Mobile BankID. Have you opened the app?")),
    ALREADY_IN_PROGRESS(
            new LocalizableKey("You have another BankID session in progress. Please try again.")),
    INTERRUPTED(
            new LocalizableKey(
                    "Another BankId authentication was initiated while authenticating. Please try again.")),
    USER_VALIDATION_ERROR(
            new LocalizableKey("Your identity could not be validated. Please try again.")),
    AUTHORIZATION_REQUIRED(new LocalizableKey("Your BankID is not authorized for this service.")),
    BANK_ID_UNAUTHORIZED_ISSUER(
            new LocalizableKey(
                    "Your BankID was not issued by the bank you are trying to authenticate with. This is not allowed by the bank. Please contact the bank to resolve this issue.")),
    BLOCKED(
            new LocalizableKey(
                    "Your BankID is blocked. Log in to the internet bank and reset/reorder BankID on mobile.")),
    UNKNOWN(new LocalizableKey("Something went wrong with the BankId authentication")),
    INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE(
            new LocalizableKey(
                    "The mobile operator you use may have blocked BankID on mobile. To resolve this, log in to the internet bank using another authentication method and reorder BankID on mobile.")), // NO
    ACTIVATE_EXTENDED_BANKID(
            new LocalizableKey(
                    "In order to add new recipients you need to activate Mobile BankID for extended use. You can find more information on how to extend your Mobile BankID in your Internet bank."));

    private final LocalizableKey userMessage;

    BankIdError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public BankIdException exception() {
        return new BankIdException(this);
    }

    @Override
    public BankIdException exception(String internalMessage) {
        return new BankIdException(this, internalMessage);
    }

    @Override
    public BankIdException exception(Throwable cause) {
        return new BankIdException(this, cause);
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public BankIdException exception(LocalizableKey userMessage) {
        return new BankIdException(this, userMessage);
    }

    @Override
    public BankIdException exception(LocalizableKey userMessage, Throwable cause) {
        return new BankIdException(this, userMessage, cause);
    }
}
