package se.tink.ediclient;

public class EdiClientException extends RuntimeException {

    public EdiClientException(String message) {
        super(message);
    }

    public EdiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
