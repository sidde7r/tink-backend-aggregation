package se.tink.backend.common.application;

public class ApplicationCannotBeDeletedException extends Exception {

    public ApplicationCannotBeDeletedException() {
        super();
    }

    public ApplicationCannotBeDeletedException(String message) {
        super(message);
    }
}
