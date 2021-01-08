package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.ThirdPartyException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.libraries.i18n.LocalizableKey;

public enum ThirdPartyError implements AgentError {
    INCORRECT_SECRETS(
            new LocalizableKey(
                    "Permission denied. Please verify if configuration of secrets you set for that financial institution is correct."));

    private final LocalizableKey userMessage;

    ThirdPartyError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public AgentException exception() {
        return new ThirdPartyException(this);
    }

    @Override
    public AgentException exception(String internalMessage) {
        return new ThirdPartyException(this, internalMessage);
    }

    @Override
    public AgentException exception(Throwable cause) {
        return new ThirdPartyException(this, cause);
    }

    @Override
    public AgentException exception(LocalizableKey userMessage) {
        return new ThirdPartyException(this, userMessage);
    }

    @Override
    public AgentException exception(LocalizableKey userMessage, Throwable cause) {
        return new ThirdPartyException(this, userMessage, cause);
    }
}
