package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.error.exceptions;

public class UnexpectedErrorException extends RuntimeException {

    public UnexpectedErrorException() {}

    public UnexpectedErrorException(String message) {
        super(message);
    }
}
