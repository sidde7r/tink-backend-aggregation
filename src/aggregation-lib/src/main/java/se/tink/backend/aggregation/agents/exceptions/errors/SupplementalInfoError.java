package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.libraries.i18n.LocalizableKey;

public enum SupplementalInfoError implements AgentError{

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
    public SupplementalInfoException exception(LocalizableKey userMessage) {
        return new SupplementalInfoException(this, userMessage);
    }
}
