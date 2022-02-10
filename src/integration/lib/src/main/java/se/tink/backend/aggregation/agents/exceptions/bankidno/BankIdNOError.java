package se.tink.backend.aggregation.agents.exceptions.bankidno;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public enum BankIdNOError implements AgentError {
    INITIALIZATION_ERROR(new LocalizableKey("Could not initialize BankID authentication.")),
    UNKNOWN_BANK_ID_ERROR(new LocalizableKey("BankID authentication error.")),
    INVALID_SSN(new LocalizableKey("Invalid social security number.")),
    INVALID_SSN_FORMAT(new LocalizableKey("Invalid social security number format.")),
    INVALID_SSN_OR_ONE_TIME_CODE(
            new LocalizableKey("Invalid social security number or one-time code.")),
    INVALID_ONE_TIME_CODE(new LocalizableKey("Invalid one-time code.")),
    INVALID_ONE_TIME_CODE_FORMAT(new LocalizableKey("Invalid one-time code format.")),
    MOBILE_BANK_ID_TIMEOUT_OR_REJECTED(
            new LocalizableKey(
                    "There was a technical error when connecting with your mobile operator.")),
    THIRD_PARTY_APP_BLOCKED(new LocalizableKey("Your BankID authentication app is blocked.")),
    THIRD_PARTY_APP_TIMEOUT(new LocalizableKey("BankID authentication app timeout.")),
    THIRD_PARTY_APP_REJECTED(
            new LocalizableKey("User rejected authentication in BankID authentication app.")),
    INVALID_BANK_ID_PASSWORD_FORMAT(new LocalizableKey("Invalid BankID password format.")),
    INVALID_BANK_ID_PASSWORD(new LocalizableKey("The BankID password is invalid."));

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
