package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;

public class PaymentAuthorizationCancelledByUserException extends PaymentAuthorizationException {
    public static final String MESSAGE =
            "Authorisation of payment was cancelled. Please try again.";

    public PaymentAuthorizationCancelledByUserException(OpenIdError openIdError) {
        super(openIdError, MESSAGE);
    }

    public PaymentAuthorizationCancelledByUserException(String message) {
        super(message);
    }

    public PaymentAuthorizationCancelledByUserException() {
        this(MESSAGE);
    }
}
