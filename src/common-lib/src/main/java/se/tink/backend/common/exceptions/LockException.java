package se.tink.backend.common.exceptions;

public class LockException extends Exception {
    public LockException() {
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(Throwable cause) {
        super(cause);
    }
}
