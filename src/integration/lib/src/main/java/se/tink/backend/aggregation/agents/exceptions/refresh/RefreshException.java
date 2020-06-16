package se.tink.backend.aggregation.agents.exceptions.refresh;

public class RefreshException extends RuntimeException {

    public RefreshException(String message) {
        super(message);
    }

    public RefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
