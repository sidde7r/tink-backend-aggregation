package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when an API call responds with <code>
 * HTTP 500 Internal Server Error</code>.
 */
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MIN_VALUE)
public class SabadellBankServiceErrorFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            // Done for COB-1505
            if (httpRequest.getURI().toString().contains("loans")) {
                return response;
            }

            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}
