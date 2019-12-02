package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception;

import se.tink.backend.aggregation.agents.exceptions.MultiFactorAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentBaseError;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public class NemIdException extends MultiFactorAuthenticationException {

    public NemIdException(AgentError error) {
        super(error);
    }

    public NemIdException(AgentError error, Throwable cause) {
        super(error, cause);
    }

    public NemIdException(AgentError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public NemIdException(AgentError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public NemIdException(AgentError error, String internalMessage) {
        super(error, internalMessage);
    }

    @Override
    public AgentBaseError getError() {
        return null;
    }
}
