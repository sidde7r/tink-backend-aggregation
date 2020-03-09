package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.exception;

public class MandatoryDataMissingException extends RuntimeException {
    public MandatoryDataMissingException() {}

    public MandatoryDataMissingException(String message) {
        super(message);
    }

    public MandatoryDataMissingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MandatoryDataMissingException(Throwable cause) {
        super(cause);
    }
}
