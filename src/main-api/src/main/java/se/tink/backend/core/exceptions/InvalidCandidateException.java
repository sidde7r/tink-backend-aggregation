package se.tink.backend.core.exceptions;

public class InvalidCandidateException extends IllegalArgumentException {
    public InvalidCandidateException(String message) {
        super(message);
    }
}
