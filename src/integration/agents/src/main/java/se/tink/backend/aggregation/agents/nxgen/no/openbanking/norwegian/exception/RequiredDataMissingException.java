package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.exception;

public class RequiredDataMissingException extends RuntimeException {

    public RequiredDataMissingException(String message) {
        super(message);
    }

    public RequiredDataMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
