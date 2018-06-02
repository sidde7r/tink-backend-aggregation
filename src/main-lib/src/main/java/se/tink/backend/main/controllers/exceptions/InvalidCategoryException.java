package se.tink.backend.main.controllers.exceptions;

public class InvalidCategoryException extends IllegalArgumentException {
    public InvalidCategoryException(String message) {
        super(message);
    }
}
