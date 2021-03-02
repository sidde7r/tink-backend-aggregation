package se.tink.backend.aggregation.agents.exceptions.nemid;

import se.tink.backend.aggregation.agents.exceptions.MultiFactorAuthenticationException;
import se.tink.libraries.i18n.LocalizableKey;

public class NemIdException extends MultiFactorAuthenticationException {

    public NemIdException(NemIdError error) {
        super(error);
    }

    public NemIdException(NemIdError error, Throwable cause) {
        super(error, cause);
    }

    public NemIdException(NemIdError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public NemIdException(NemIdError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public NemIdException(NemIdError error, String internalMessage) {
        super(error, internalMessage);
    }

    @Override
    public NemIdError getError() {
        return getError(NemIdError.class);
    }
}
