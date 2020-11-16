package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public enum SupplementalInfoError implements AgentError {
    WAIT_TIMEOUT(
            new LocalizableKey(
                    "A supplemental information was not provided within the given time")),
    NO_VALID_CODE(new LocalizableKey("You have not entered a valid code. Please try again"));

    private LocalizableKey userMessage;

    SupplementalInfoError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public LocalizableKey userMessage() {
        return this.userMessage;
    }

    @Override
    public SupplementalInfoException exception() {
        return new SupplementalInfoException(this);
    }

    @Override
    public SupplementalInfoException exception(String internalMessage) {
        return new SupplementalInfoException(this, internalMessage);
    }

    @Override
    public SupplementalInfoException exception(Throwable cause) {
        return new SupplementalInfoException(this, cause);
    }

    @Override
    public SupplementalInfoException exception(LocalizableKey userMessage) {
        return new SupplementalInfoException(this, userMessage);
    }

    @Override
    public SupplementalInfoException exception(LocalizableKey userMessage, Throwable cause) {
        return new SupplementalInfoException(this, userMessage, cause);
    }
}
