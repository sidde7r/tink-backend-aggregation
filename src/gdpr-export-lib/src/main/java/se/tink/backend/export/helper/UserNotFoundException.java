package se.tink.backend.export.helper;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(String errorMessage){
        super(errorMessage);
    }
}
