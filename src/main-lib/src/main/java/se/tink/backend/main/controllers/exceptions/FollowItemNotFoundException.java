package se.tink.backend.main.controllers.exceptions;

public class FollowItemNotFoundException extends Exception {
    public FollowItemNotFoundException(String id) {
        super(String.format("Follow item with id = %s was not found", id));
    }
}
