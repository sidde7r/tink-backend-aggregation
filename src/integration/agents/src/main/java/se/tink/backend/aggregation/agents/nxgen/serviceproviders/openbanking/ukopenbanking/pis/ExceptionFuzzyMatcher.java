package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;

public class ExceptionFuzzyMatcher {
    private static final String AUTHORIZATION_CANCELLED_BY_USER = "cancelled";
    private static final String AUTHORIZATION_FAILED_BY_USER = "User failed to authenticate";
    private static final String AUTHORIZATION_TIME_OUT = "not completed in the allotted time";

    public boolean isAuthorizationCancelledByUser(ErrorEntity error) {
        return StringUtils.containsIgnoreCase(
                error.getErrorMessage(), AUTHORIZATION_CANCELLED_BY_USER);
    }

    public boolean isAuthorizationFailedByUser(ErrorEntity error) {
        return StringUtils.containsIgnoreCase(
                error.getErrorMessage(), AUTHORIZATION_FAILED_BY_USER);
    }

    public boolean isAuthorizationTimeOut(ErrorEntity error) {
        return StringUtils.containsIgnoreCase(error.getErrorMessage(), AUTHORIZATION_TIME_OUT);
    }
}
