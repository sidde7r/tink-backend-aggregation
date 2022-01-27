package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

public class MissingLocationException extends RuntimeException {

    public MissingLocationException(String message) {
        super(message);
    }
}
