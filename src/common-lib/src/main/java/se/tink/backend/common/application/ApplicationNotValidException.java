package se.tink.backend.common.application;

public class ApplicationNotValidException extends Exception {

    public ApplicationNotValidException(String message) {
        super(message);
    }
}
