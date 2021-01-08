package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyError;
import se.tink.libraries.i18n.LocalizableKey;

public class ThirdPartyException extends AgentException {

    public ThirdPartyException(ThirdPartyError error) {
        super(error);
    }

    public ThirdPartyException(ThirdPartyError error, Throwable cause) {
        super(error, cause);
    }

    public ThirdPartyException(ThirdPartyError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public ThirdPartyException(ThirdPartyError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public ThirdPartyException(ThirdPartyError thirdPartyAppError, String internalMessage) {
        super(thirdPartyAppError, internalMessage);
    }

    @Override
    public ThirdPartyError getError() {
        return getError(ThirdPartyError.class);
    }
}
