package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.libraries.i18n.LocalizableKey;

public class ThirdPartyAppException extends MultiFactorAuthenticationException {

    private final ThirdPartyAppError error;

    public ThirdPartyAppException(ThirdPartyAppError error) {
        super(error);
        this.error = error;
    }

    public ThirdPartyAppException(ThirdPartyAppError error, LocalizableKey userMessage) {
        super(error, userMessage);
        this.error = error;
    }

    @Override
    public ThirdPartyAppError getError() {
        return error;
    }
}
