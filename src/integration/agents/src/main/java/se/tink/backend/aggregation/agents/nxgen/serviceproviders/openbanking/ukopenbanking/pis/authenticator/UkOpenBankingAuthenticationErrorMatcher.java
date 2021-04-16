package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;

public class UkOpenBankingAuthenticationErrorMatcher {

    private static final String AUTHORIZATION_CANCELLED_BY_USER = "cancelled";
    private static final String AUTHORIZATION_REJECTED_BY_USER = "User rejection";
    private static final String CONSENT_REJECTED_BY_USER = "rejected";
    private static final String AUTHORIZATION_FAILED_BY_USER = "User failed to authenticate";
    private static final String AUTHORIZATION_TIME_OUT = "not completed in the allotted time";
    private static final String CONSENT_DENIED_BY_USER = "The user denied consent.";
    private static final String NATIONWIDE_USER_CANCEL_MSG = "access_denied";

    boolean isAuthorizationCancelledByUser(String errorMessage) {
        return StringUtils.containsIgnoreCase(errorMessage, AUTHORIZATION_CANCELLED_BY_USER)
                || StringUtils.containsIgnoreCase(errorMessage, CONSENT_REJECTED_BY_USER)
                || StringUtils.containsIgnoreCase(errorMessage, AUTHORIZATION_REJECTED_BY_USER)
                || StringUtils.containsIgnoreCase(errorMessage, CONSENT_DENIED_BY_USER)
                || StringUtils.containsIgnoreCase(errorMessage, NATIONWIDE_USER_CANCEL_MSG);
    }

    boolean isAuthorizationFailedByUser(String errorMessage) {
        return StringUtils.containsIgnoreCase(errorMessage, AUTHORIZATION_FAILED_BY_USER);
    }

    boolean isAuthorizationTimeOut(String errorMessage) {
        return StringUtils.containsIgnoreCase(errorMessage, AUTHORIZATION_TIME_OUT);
    }

    boolean isKnownOpenIdError(String errorType) {
        return OpenIdConstants.Errors.ACCESS_DENIED.equalsIgnoreCase(errorType)
                || OpenIdConstants.Errors.LOGIN_REQUIRED.equalsIgnoreCase(errorType);
    }
}
