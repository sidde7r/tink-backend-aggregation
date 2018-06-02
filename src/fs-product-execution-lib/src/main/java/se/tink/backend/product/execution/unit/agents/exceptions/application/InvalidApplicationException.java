package se.tink.backend.product.execution.unit.agents.exceptions.application;

public class InvalidApplicationException extends Exception {
    public InvalidApplicationException() {
        super();
    }

    public InvalidApplicationException(String userMessage) {
        super(userMessage);
    }
}
