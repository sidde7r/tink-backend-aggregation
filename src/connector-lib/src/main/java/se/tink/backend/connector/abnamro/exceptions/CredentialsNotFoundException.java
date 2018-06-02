package se.tink.backend.connector.abnamro.exceptions;

public class CredentialsNotFoundException extends Exception {
    public CredentialsNotFoundException(Long bcNumber) {
        super(String.format("ABN AMRO credential not found (BcNumber = '%d').", bcNumber));
    }
}
