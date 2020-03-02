package se.tink.backend.aggregation.agents.exceptions.payment;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;

public class PaymentAuthorizationFailedByUserException extends PaymentAuthorizationException {
    public static final String MESSAGE = "Authorisation of payment failed. Please try again.";
    private static final String searchString = "User failed to authenticate";

    public PaymentAuthorizationFailedByUserException(OpenIdError openIdError) {
        super(openIdError, MESSAGE);
    }

    public static boolean isFuzzyMatch(OpenIdError error) {
        return StringUtils.containsIgnoreCase(error.getErrorMessage(), searchString);
    }
}
