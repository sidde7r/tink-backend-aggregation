package se.tink.backend.aggregation.agents.creditcards.ikano.api.errors;

public class FatalErrorException extends Exception {

    public FatalErrorException(String message) {
        super(message);
    }
}
