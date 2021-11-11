package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CmicResponseStatusHandler extends DefaultResponseStatusHandler {

    private static final Set<String> ACCESS_IS_REJECTED_ERRORS =
            Sets.newHashSet("All access is rejected.", "No access is allowed");

    private static final String NO_ACCOUNTS = "The specified user has no payment accounts";

    private static final String SERVICE_UNAVAILABLE = "Service unavailable";
    private static final String INTERNAL_ERROR = "Internal Server Error";

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isAccessTokenBlocked(httpResponse)) {
            throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception();
        }
        if (isNoAccountsError(httpResponse)) {
            throw LoginError.NO_ACCOUNTS.exception();
        }
        if (isServiceUnavailable(httpResponse)) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        if (isInternalServerError(httpResponse)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isAccessTokenBlocked(HttpResponse httpResponse) {
        String body = httpResponse.getBody(String.class);
        return (httpResponse.getStatus() == 403
                && (ACCESS_IS_REJECTED_ERRORS.stream().anyMatch(body::contains)));
    }

    private boolean isServiceUnavailable(HttpResponse httpResponse) {
        String body = httpResponse.getBody(String.class);
        return (httpResponse.getStatus() == 503 && (body.contains(SERVICE_UNAVAILABLE)));
    }

    private boolean isNoAccountsError(HttpResponse httpResponse) {
        String body = httpResponse.getBody(String.class);
        return (httpResponse.getStatus() == 403 && (body.contains(NO_ACCOUNTS)));
    }

    private boolean isInternalServerError(HttpResponse httpResponse) {
        String body = httpResponse.getBody(String.class);
        return (httpResponse.getStatus() == 500 && (body.contains(INTERNAL_ERROR)));
    }
}
