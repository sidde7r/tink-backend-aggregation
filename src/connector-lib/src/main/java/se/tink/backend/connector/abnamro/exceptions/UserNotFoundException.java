package se.tink.backend.connector.abnamro.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(long bcNumber) {
        super(String.format("User not found (BcNumber = '%d').", bcNumber));
    }
}

