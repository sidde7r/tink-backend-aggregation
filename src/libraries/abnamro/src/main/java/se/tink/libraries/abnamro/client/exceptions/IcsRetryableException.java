package se.tink.libraries.abnamro.client.exceptions;

public class IcsRetryableException extends IcsException {

    public IcsRetryableException(String message) {
        super(message);
    }

    public IcsRetryableException(String key, String message) {
        super(key, message);
    }
}
