package se.tink.backend.product.execution.unit.agents.exceptions.errors;

import se.tink.backend.product.execution.unit.agents.exceptions.BankIdException;
import se.tink.libraries.i18n.LocalizableKey;

public enum BankIdError implements AgentError {
    CANCELLED(new LocalizableKey("You cancelled the BankID process. Please try again.")),
    TIMEOUT(new LocalizableKey("No response from Mobile BankID. Have you opened the app?")),
    NO_CLIENT(new LocalizableKey("No response from Mobile BankID. Have you opened the app?")),
    ALREADY_IN_PROGRESS(new LocalizableKey("You have another BankID session in progress. Please try again.")),
    INTERRUPTED(new LocalizableKey("Another BankId authentication was initiated while authenticating. Please try again.")),
    USER_VALIDATION_ERROR(new LocalizableKey("Your identity could not be validated. Please try again.")),
    AUTHORIZATION_REQUIRED(new LocalizableKey("Your BankID is not authorized for this service.")),
    BLOCKED(new LocalizableKey("Your BankID is blocked. Log in to the internet bank and reset/reorder BankID on mobile.")),
    UNKNOWN(new LocalizableKey("Something went wrong with the BankId authentication")),
    INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE(new LocalizableKey("The mobile operator you use may have blocked BankID on mobile. To resolve this, log in to the internet bank using another authentication method and reorder BankID on mobile.")); // NO

    private final LocalizableKey userMessage;

    BankIdError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public BankIdException exception() {
        return new BankIdException(this);
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public BankIdException exception(LocalizableKey userMessage) {
        return new BankIdException(this, userMessage);
    }
}
