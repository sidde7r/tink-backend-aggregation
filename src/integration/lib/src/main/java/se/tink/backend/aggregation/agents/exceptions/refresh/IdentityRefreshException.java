package se.tink.backend.aggregation.agents.exceptions.refresh;

public class IdentityRefreshException extends RefreshException {

    public IdentityRefreshException(String message) {
        super(message);
    }

    public IdentityRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
