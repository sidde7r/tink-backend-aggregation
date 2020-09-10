package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.validator;

public class IdTokenValidationException extends RuntimeException {

    private static final String PREFIX = "ID Token validation failed: ";

    public IdTokenValidationException(String message) {
        super(PREFIX + message);
    }

    public IdTokenValidationException(String message, Throwable cause) {
        super(PREFIX + message, cause);
    }
}
