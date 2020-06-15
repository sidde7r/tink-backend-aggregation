package se.tink.backend.aggregation.agents.exceptions.refresh;

public class LoanAccountRefreshException extends AccountRefreshException {

    public LoanAccountRefreshException(String message) {
        super(message);
    }

    public LoanAccountRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
