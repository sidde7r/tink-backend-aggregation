package se.tink.backend.aggregation.agents.exceptions.refresh;

public class CheckingAccountRefreshException extends AccountRefreshException {

    public CheckingAccountRefreshException(String message) {
        super(message);
    }

    public CheckingAccountRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
