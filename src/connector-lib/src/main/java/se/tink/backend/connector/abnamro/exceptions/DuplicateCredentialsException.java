package se.tink.backend.connector.abnamro.exceptions;

public class DuplicateCredentialsException extends Exception {
    public DuplicateCredentialsException(Long bcNumber) {
        super(String.format("Duplicate ABN AMRO credentials found (BcNumber = '%d').", bcNumber));
    }
}
