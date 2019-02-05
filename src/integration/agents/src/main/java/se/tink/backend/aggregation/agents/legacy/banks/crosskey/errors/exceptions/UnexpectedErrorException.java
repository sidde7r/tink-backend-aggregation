package se.tink.backend.aggregation.agents.banks.crosskey.errors.exceptions;

public class UnexpectedErrorException extends RuntimeException {

    public UnexpectedErrorException() {}

    public UnexpectedErrorException(String message) {
        super(message);
    }
}
