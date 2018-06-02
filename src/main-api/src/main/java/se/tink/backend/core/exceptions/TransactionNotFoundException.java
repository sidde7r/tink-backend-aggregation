package se.tink.backend.core.exceptions;

public class TransactionNotFoundException extends IllegalArgumentException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
