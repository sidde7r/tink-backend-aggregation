package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;

public class ExceptionFuzzyMatcher {
    private static final String AUTHORIZATION_CANCELLED_BY_USER = "cancelled";
    private static final String AUTHORIZATION_FAILED_BY_USER = "User failed to authenticate";
    private static final String AUTHORIZATION_TIME_OUT = "not completed in the allotted time";

    public boolean isAuthorizationCancelledByUser(OpenIdError error) {
        return StringUtils.containsIgnoreCase(
                error.getErrorMessage(), AUTHORIZATION_CANCELLED_BY_USER);
    }

    public boolean isAuthorizationFailedByUser(OpenIdError error) {
        return StringUtils.containsIgnoreCase(
                error.getErrorMessage(), AUTHORIZATION_FAILED_BY_USER);
    }

    public boolean isAuthorizationTimeOut(OpenIdError error) {
        return StringUtils.containsIgnoreCase(error.getErrorMessage(), AUTHORIZATION_TIME_OUT);
    }
}
