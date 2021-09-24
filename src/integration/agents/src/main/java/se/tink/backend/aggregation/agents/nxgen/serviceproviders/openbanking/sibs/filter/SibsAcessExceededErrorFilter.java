package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when an SIBS API call responds with
 * <code>
 * HTTP 429 ACCESS_EXCEEDED</code>.
 *
 * <p>Typical response body:
 *
 * <p>{"transactionStatus":"RJCT","tppMessages":[{"category":"ERROR","code":"ACCESS_EXCEEDED","text":"The
 * access on the account has been exceeding the consented multiplicity per day."}]}
 */
public class SibsAcessExceededErrorFilter extends Filter {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final String ACCESS_EXCEEDED_CODE = "ACCESS_EXCEEDED";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == TOO_MANY_REQUESTS
                && response.hasBody()
                && (response.getBody(String.class).contains(ACCESS_EXCEEDED_CODE))) {
            throw BankServiceError.ACCESS_EXCEEDED.exception(
                    "Http status: "
                            + response.getStatus()
                            + " Error body: "
                            + response.getBody(String.class));
        }

        return response;
    }
}
