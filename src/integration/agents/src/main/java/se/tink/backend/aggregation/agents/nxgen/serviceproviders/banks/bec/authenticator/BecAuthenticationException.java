package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

public class BecAuthenticationException extends RuntimeException {

    public BecAuthenticationException(final String message) {
        super(message);
    }
}
