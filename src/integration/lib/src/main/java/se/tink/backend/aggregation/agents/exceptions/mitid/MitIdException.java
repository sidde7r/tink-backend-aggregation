package se.tink.backend.aggregation.agents.exceptions.mitid;

import se.tink.backend.aggregation.agents.exceptions.MultiFactorAuthenticationException;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class MitIdException extends MultiFactorAuthenticationException {

    public MitIdException(MitIdError error) {
        super(error);
    }

    public MitIdException(MitIdError error, Throwable cause) {
        super(error, cause);
    }

    public MitIdException(MitIdError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public MitIdException(MitIdError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public MitIdException(MitIdError error, String internalMessage) {
        super(error, internalMessage);
    }

    @Override
    public MitIdError getError() {
        return getError(MitIdError.class);
    }
}
