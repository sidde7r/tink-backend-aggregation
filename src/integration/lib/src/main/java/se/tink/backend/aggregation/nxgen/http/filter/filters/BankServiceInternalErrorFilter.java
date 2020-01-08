package se.tink.backend.aggregation.nxgen.http.filter.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when an API call responds with <code>
 * HTTP 500 Internal Server Error</code>.
 */
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MIN_VALUE)
public class BankServiceInternalErrorFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}
