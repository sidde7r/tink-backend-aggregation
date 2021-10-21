package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.error;

public class FTSException extends RuntimeException {
    public FTSException(String message) {
        super(message);
    }
}
