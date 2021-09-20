package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CmicResponseStatusHandler extends DefaultResponseStatusHandler {

    private static final String ACCESS_TOKEN_BLOCKED =
            "The user account associated with this access token has been blocked. All access is rejected.";

    private static final String ACCESS_TOKEN_BLOCKED_BY_ACCOUNT_MANAGER =
            "The user account associated with this access token has been blocked by an account manager. All access is rejected.";

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isAccessTokenBlocked(httpResponse)) {
            throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isAccessTokenBlocked(HttpResponse httpResponse) {
        String body = httpResponse.getBody(String.class);
        return (httpResponse.getStatus() == 403
                && (body.contains(ACCESS_TOKEN_BLOCKED)
                        || body.contains(ACCESS_TOKEN_BLOCKED_BY_ACCOUNT_MANAGER)));
    }
}
