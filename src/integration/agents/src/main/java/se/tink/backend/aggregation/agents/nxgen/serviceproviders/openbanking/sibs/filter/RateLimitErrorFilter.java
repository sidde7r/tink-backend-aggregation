package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when an SIBS API call responds with
 * <code>
 * HTTP 429 Too many requests</code>.
 *
 * <p>Typical response body:
 *
 * <p>{ "httpCode":"429", "httpMessage":"Too Many Requests", "moreInformation":"Rate Limit exceeded"
 * }
 */
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MIN_VALUE)
public class RateLimitErrorFilter extends Filter {

    private static final int TOO_MANY_REQUESTS = 429;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == TOO_MANY_REQUESTS) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception("Http status: " + TOO_MANY_REQUESTS);
        }

        return response;
    }
}
