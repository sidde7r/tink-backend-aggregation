package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofscotland.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BankOfScotlandErrorHandlerFilter extends Filter {

    private static final String ACCOUNT_CLOSED_OR_SUSPENDED = "Account closed or suspended";
    private static final String CODE_ACCOUNT_CLOSED_OR_SUSPENDED = "UK.OBIE.Resource.NotFound";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (isAccountClosed(response)) {
            throw new AccountRefreshException(ACCOUNT_CLOSED_OR_SUSPENDED);
        }

        return response;
    }

    private boolean isAccountClosed(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_FORBIDDEN && notFoundAccount(response);
    }

    private boolean notFoundAccount(HttpResponse response) {
        return response.getBody(ErrorResponse.class).hasErrorCode(CODE_ACCOUNT_CLOSED_OR_SUSPENDED);
    }
}
