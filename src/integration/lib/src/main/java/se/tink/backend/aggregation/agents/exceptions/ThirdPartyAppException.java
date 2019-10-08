package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.libraries.i18n.LocalizableKey;

public class ThirdPartyAppException extends MultiFactorAuthenticationException {

    public ThirdPartyAppException(ThirdPartyAppError error) {
        super(error);
    }

    public ThirdPartyAppException(ThirdPartyAppError error, Throwable cause) {
        super(error, cause);
    }

    public ThirdPartyAppException(ThirdPartyAppError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public ThirdPartyAppException(
            ThirdPartyAppError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    @Override
    public ThirdPartyAppError getError() {
        return getError(ThirdPartyAppError.class);
    }
}
