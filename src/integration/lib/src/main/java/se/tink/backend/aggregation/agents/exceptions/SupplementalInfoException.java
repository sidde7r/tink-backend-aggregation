package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class SupplementalInfoException extends AuthenticationException {

    public SupplementalInfoException(SupplementalInfoError error) {
        super(error);
    }

    public SupplementalInfoException(SupplementalInfoError error, Throwable cause) {
        super(error, cause);
    }

    public SupplementalInfoException(SupplementalInfoError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public SupplementalInfoException(
            SupplementalInfoError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public SupplementalInfoException(
            SupplementalInfoError supplementalInfoError, String internalMessage) {
        super(supplementalInfoError, internalMessage);
    }

    @Override
    public SupplementalInfoError getError() {
        return getError(SupplementalInfoError.class);
    }
}
