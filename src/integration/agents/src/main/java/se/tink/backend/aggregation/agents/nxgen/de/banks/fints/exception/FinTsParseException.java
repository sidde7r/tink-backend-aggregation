package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.exception;

public class FinTsParseException extends RuntimeException {
    public FinTsParseException(String s) {
        super(s);
    }

    public FinTsParseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FinTsParseException(Throwable throwable) {
        super(throwable);
    }
}
