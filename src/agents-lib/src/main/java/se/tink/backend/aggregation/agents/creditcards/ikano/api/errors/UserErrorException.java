package se.tink.backend.aggregation.agents.creditcards.ikano.api.errors;

public class UserErrorException extends Exception {

    public UserErrorException(String message) {
        super(message);
    }
}
