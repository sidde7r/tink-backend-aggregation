package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.session.utils;

import se.tink.backend.aggregation.agents.exceptions.errors.AgentBaseError;
import se.tink.libraries.i18n.LocalizableKey;

// TODO: Work on correct implementation of this class
public class UnknownError implements AgentBaseError {

    private final RuntimeException exception;
    private final LocalizableKey key;

    public UnknownError(RuntimeException ex) {
        this.exception = ex;
        this.key = new LocalizableKey("Unknown error");
    }

    @Override
    public String name() {
        return "Unknown Error";
    }

    @Override
    public LocalizableKey userMessage() {
        return new LocalizableKey("Unknown error");
    }

    @Override
    public RuntimeException exception() {
        return this.exception;
    }

    @Override
    public RuntimeException exception(LocalizableKey userMessage) {
        return new RuntimeException(key.toString(), exception);
    }
}
