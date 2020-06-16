package se.tink.backend.aggregation.agents.exceptions.refresh;

public class InvestmentAccountRefreshException extends AccountRefreshException {

    public InvestmentAccountRefreshException(String message) {
        super(message);
    }

    public InvestmentAccountRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
