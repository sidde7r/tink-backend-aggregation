package se.tink.backend.main.validators.exception;

public class InstantiationException extends RuntimeException {
    public InstantiationException(Object instance, String reason) {
        super(String.format("Failed to instantiate %s ( %s )",
                instance.getClass().getSimpleName(), reason));
    }
}
