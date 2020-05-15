package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.exception;

public class ResponseParseException extends RuntimeException {

    public ResponseParseException() {}

    public ResponseParseException(String message) {
        super(message);
    }

    public ResponseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResponseParseException(Throwable cause) {
        super(cause);
    }
}
