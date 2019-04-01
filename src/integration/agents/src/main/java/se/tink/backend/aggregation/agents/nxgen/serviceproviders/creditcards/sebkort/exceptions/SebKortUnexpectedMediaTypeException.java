package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.exceptions;

public class SebKortUnexpectedMediaTypeException extends RuntimeException {

    private final String body;

    public SebKortUnexpectedMediaTypeException(String body) {
        super();

        this.body = body;
    }

    @Override
    public String toString() {
        return super.toString() + " Response body: " + body;
    }
}
