package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;

public class PaymentAuthorizationTimeOutException extends PaymentAuthorizationException {
    public static final String MESSAGE = "Authorisation of payment timed out. Please try again.";

    public PaymentAuthorizationTimeOutException(String message) {
        super(message);
    }

    public PaymentAuthorizationTimeOutException() {
        this(MESSAGE);
    }

    public PaymentAuthorizationTimeOutException(OpenIdError openIdError) {
        super(openIdError, MESSAGE);
    }
}
