package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.exception;

public class UnsupportedCurrencyException extends RuntimeException {
    public UnsupportedCurrencyException() {}

    public UnsupportedCurrencyException(String message) {
        super(message);
    }

    public UnsupportedCurrencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedCurrencyException(Throwable cause) {
        super(cause);
    }
}
