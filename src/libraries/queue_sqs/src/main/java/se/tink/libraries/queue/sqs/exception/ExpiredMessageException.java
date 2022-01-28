package se.tink.libraries.queue.sqs.exception;

public class ExpiredMessageException extends RuntimeException {

    public ExpiredMessageException(String message) {
        super(message);
    }

    public ExpiredMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
