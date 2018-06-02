package se.tink.backend.main.auth.exceptions;

public class UnsupportedClientException extends RuntimeException {
    private final String clientKey;

    public UnsupportedClientException(String message) {
        this(message, null);
    }

    public UnsupportedClientException(String message, String clientKey) {
        super(message);
        this.clientKey = clientKey;
    }

    public String getClientKey() {
        return clientKey;
    }
}
