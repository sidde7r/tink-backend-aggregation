package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class AccountBlockedOrSuspendedValidator {

    private static final String RESOURCE_NOT_FOUND = "UK.OBIE.Resource.NotFound";
    private static final String CODE_ACCOUNT_CLOSED_OR_SUSPENDED =
            "Forbidden - Account closed or suspended";

    public static boolean isAccountClosed(HttpResponse response) {
        return isStatus403(response)
                && isErrorCodeResourceNotFound(response)
                && isMessageAccountClosedOrSuspended(response);
    }

    private static boolean isStatus403(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_FORBIDDEN;
    }

    private static boolean isErrorCodeResourceNotFound(HttpResponse response) {
        return response.getBody(ErrorResponse.class).hasErrorCode(RESOURCE_NOT_FOUND);
    }

    private static boolean isMessageAccountClosedOrSuspended(HttpResponse response) {
        return response.getBody(ErrorResponse.class)
                .messageContains(CODE_ACCOUNT_CLOSED_OR_SUSPENDED);
    }
}
