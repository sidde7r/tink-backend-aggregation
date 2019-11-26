package se.tink.sa.framework.common.exceptions;

public class StandaloneAgentException extends RuntimeException {

    public StandaloneAgentException() {}

    public StandaloneAgentException(String message) {
        super(message);
    }

    public StandaloneAgentException(String message, Throwable cause) {
        super(message, cause);
    }

    public StandaloneAgentException(Throwable cause) {
        super(cause);
    }

    public StandaloneAgentException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
