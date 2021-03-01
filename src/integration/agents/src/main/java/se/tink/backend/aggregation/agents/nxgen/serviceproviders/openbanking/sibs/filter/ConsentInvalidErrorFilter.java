package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when an SIBS API call responds with
 * <code>
 * HTTP 401 Unauthorized</code>.
 *
 * <p>Typical response body:
 *
 * <p>{"transactionStatus":"RJCT","tppMessages":[{"category":"ERROR","code":"CONSENT_INVALID","text":"The
 * consent definition is not complete or invalid. In case of being not complete, the bank is not
 * supporting a completion of the consent towards the PSU. Additional information will be
 * provided."}]}
 */
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MIN_VALUE)
public final class ConsentInvalidErrorFilter extends Filter {

    private static final int CONSENT_INVALID = 401;

    @Override
    public HttpResponse handle(final HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        final HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == CONSENT_INVALID) {
            String body = response.getBody(String.class);
            throw SessionError.CONSENT_INVALID.exception(
                    "Http status: " + response.getStatus() + " Error body: " + body);
        }

        return response;
    }
}
