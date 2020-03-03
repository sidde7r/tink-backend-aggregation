package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;

public class PaymentAuthorizationFailedByUserException extends PaymentAuthorizationException {
    public static final String MESSAGE = "Authorisation of payment failed. Please try again.";

    public PaymentAuthorizationFailedByUserException(OpenIdError openIdError) {
        super(openIdError, MESSAGE);
    }
}
