package se.tink.backend.product.execution.unit.agents.exceptions;

import se.tink.backend.product.execution.unit.agents.exceptions.errors.AgentError;
import se.tink.backend.product.execution.unit.agents.exceptions.errors.SupplementalInfoError;
import se.tink.libraries.i18n.LocalizableKey;

public class SupplementalInfoException extends AuthenticationException {

    private final SupplementalInfoError error;

    public SupplementalInfoException(SupplementalInfoError error) {
        super(error);
        this.error = error;
    }

    public SupplementalInfoException(SupplementalInfoError error, LocalizableKey userMessage) {
        super(error, userMessage);
        this.error = error;
    }
    @Override
    public AgentError getError() {
        return error;
    }
}
