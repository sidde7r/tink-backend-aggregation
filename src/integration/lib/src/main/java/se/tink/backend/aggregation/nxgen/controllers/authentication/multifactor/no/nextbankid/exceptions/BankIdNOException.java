package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.exceptions;

import se.tink.backend.aggregation.agents.exceptions.MultiFactorAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public class BankIdNOException extends MultiFactorAuthenticationException {

    public BankIdNOException(BankIdNOError error) {
        super(error);
    }

    public BankIdNOException(BankIdNOError error, Throwable cause) {
        super(error, cause);
    }

    public BankIdNOException(BankIdNOError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public BankIdNOException(BankIdNOError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public BankIdNOException(AgentError error, String internalMessage) {
        super(error, internalMessage);
    }

    @Override
    public BankIdNOError getError() {
        return getError(BankIdNOError.class);
    }
}
