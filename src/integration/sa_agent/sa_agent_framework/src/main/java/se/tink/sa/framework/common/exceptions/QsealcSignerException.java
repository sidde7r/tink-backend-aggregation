package se.tink.sa.framework.common.exceptions;

public class QsealcSignerException extends StandaloneAgentException {

    public QsealcSignerException() {}

    public QsealcSignerException(String message) {
        super(message);
    }

    public QsealcSignerException(String message, Throwable cause) {
        super(message, cause);
    }

    public QsealcSignerException(Throwable cause) {
        super(cause);
    }

    public QsealcSignerException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
