package se.tink.backend.aggregation.agents.abnamro.client.exceptions;

public class IcsRetryableException extends IcsException {

    public IcsRetryableException(String message) {
        super(message);
    }

    public IcsRetryableException(String key, String message) {
        super(key, message);
    }
}
