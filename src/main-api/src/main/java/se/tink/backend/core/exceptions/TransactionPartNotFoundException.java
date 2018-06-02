package se.tink.backend.core.exceptions;

public class TransactionPartNotFoundException extends IllegalArgumentException {
    public TransactionPartNotFoundException(String message) {
        super(message);
    }
}

