package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentAuthorizationException extends PaymentException {
    public static final String DEFAULT_MESSAGE = "Payment was not authorised. Please try again.";
    protected OpenIdError openIdError;

    public PaymentAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentAuthorizationException(String message) {
        super(message);
    }

    public PaymentAuthorizationException() {
        super(DEFAULT_MESSAGE);
    }

    public PaymentAuthorizationException(OpenIdError openIdError, String message) {
        super(message);
        this.openIdError = openIdError;
    }

    public PaymentAuthorizationException(String message, InternalStatus internalStatus) {
        super(message, internalStatus);
    }

    public PaymentAuthorizationException(
            String message, InternalStatus internalStatus, AuthenticationException cause) {
        super(message, internalStatus, cause);
    }
}
