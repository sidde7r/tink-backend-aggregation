package se.tink.backend.aggregation.agents.exceptions.payment;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;

public class PaymentAuthorizationTimeOutException extends PaymentAuthorizationException {
    public static final String MESSAGE = "Authorisation of payment timed out. Please try again.";
    private static final String searchString = "not completed in the allotted time";

    public PaymentAuthorizationTimeOutException(String message) {
        super(message);
    }

    public PaymentAuthorizationTimeOutException() {
        this(MESSAGE);
    }

    public PaymentAuthorizationTimeOutException(OpenIdError openIdError) {
        super(openIdError, MESSAGE);
    }

    public static boolean isFuzzyMatch(OpenIdError error) {
        return StringUtils.containsIgnoreCase(error.getErrorMessage(), searchString);
    }
}
