package se.tink.backend.aggregation.agents.exceptions.bankidno;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public enum BankIdNOError implements AgentError {
    INITIALIZATION_ERROR(new LocalizableKey("Could not initialize BankID authentication.")),
    UNKNOWN_BANK_ID_ERROR(new LocalizableKey("BankID authentication error.")),
    INVALID_SSN_OR_ONE_TIME_CODE(
            new LocalizableKey("Invalid social security number or one-time code.")),
    MOBILE_BANK_ID_TIMEOUT_OR_REJECTED(
            new LocalizableKey(
                    "There was a technical error when connecting with your mobile operator.")),
    BANK_ID_APP_BLOCKED(new LocalizableKey("Your BankID app is blocked.")),
    BANK_ID_APP_TIMEOUT(new LocalizableKey("BankID app timeout.")),
    BANK_ID_APP_REJECTED(new LocalizableKey("The BankID app authentication was rejected by user.")),
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
