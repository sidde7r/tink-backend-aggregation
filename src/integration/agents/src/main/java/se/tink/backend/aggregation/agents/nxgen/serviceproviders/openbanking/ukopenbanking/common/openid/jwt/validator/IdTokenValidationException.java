package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.validator;

public class IdTokenValidationException extends RuntimeException {

    IdTokenValidationException(String message) {
        super(message);
    }

    IdTokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
