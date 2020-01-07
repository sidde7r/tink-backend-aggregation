package se.tink.backend.aggregation.nxgen.http.filter.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when an API call responds with <code>
 * HTTP 504 Gateway Timeout</code>.
 */
public class GatewayTimeoutFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT) {
            throw BankServiceError.NO_BANK_SERVICE.exception(
                    "Http status: " + HttpStatus.SC_GATEWAY_TIMEOUT);
        }

        return response;
    }
}
