package se.tink.backend.aggregation.eidassigner;

public class QsealcSignerException extends RuntimeException {

    public QsealcSignerException(String message) {
        super(message);
    }

    public QsealcSignerException(String message, Throwable cause) {
        super(message, cause);
    }
}
