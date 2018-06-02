package se.tink.backend.main.controllers.abnamro.exceptions;

public class UserAlreadyMigratedException extends Exception {
    public UserAlreadyMigratedException(String userName) {
        super(String.format("User already migrated. (Username = '%s')", userName));
    }
}
