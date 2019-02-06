package se.tink.backend.aggregation.agents.exceptions.application;

public class InvalidApplicationException extends Exception {
    public InvalidApplicationException() {
        super();
    }

    public InvalidApplicationException(String userMessage) {
        super(userMessage);
    }
}
