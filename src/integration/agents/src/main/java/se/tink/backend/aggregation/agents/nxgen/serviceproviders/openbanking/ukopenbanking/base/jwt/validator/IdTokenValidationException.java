package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.validator;

public class IdTokenValidationException extends RuntimeException {

    private static final String PREFIX = "ID Token validation failed: ";

    IdTokenValidationException(String message) {
        super(PREFIX + message);
    }

    IdTokenValidationException(String message, Throwable cause) {
        super(PREFIX + message, cause);
    }
}
