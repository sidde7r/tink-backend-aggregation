package se.tink.backend.aggregation.agents.abnamro.client.exceptions;

public class IcsException extends Exception {

    private String key;

    public IcsException(String message) {
        super(message);
    }

    public IcsException(String key, String message) {
        super(
                String.format(
                        "Could not fetch transactions (Key = '%s', Message = '%s')", key, message));
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
