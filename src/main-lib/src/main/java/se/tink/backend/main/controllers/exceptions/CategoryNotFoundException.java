package se.tink.backend.main.controllers.exceptions;

public class CategoryNotFoundException extends IllegalArgumentException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
