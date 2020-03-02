package se.tink.backend.aggregation.agents.exceptions.payment;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;

public class PaymentAuthorizationCancelledByUserException extends PaymentAuthorizationException {
    public static final String MESSAGE =
            "Authorisation of payment was cancelled. Please try again.";
    private static final String searchString = "cancelled";

    public PaymentAuthorizationCancelledByUserException(OpenIdError openIdError) {
        super(openIdError, MESSAGE);
    }

    public static boolean isFuzzyMatch(OpenIdError error) {
        return StringUtils.containsIgnoreCase(error.getErrorMessage(), searchString);
    }
}
