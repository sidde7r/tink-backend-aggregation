package se.tink.backend.aggregation.agents.exceptions.refresh;

public class AccountRefreshException extends RefreshException {

    public AccountRefreshException(String message) {
        super(message);
    }

    public AccountRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
