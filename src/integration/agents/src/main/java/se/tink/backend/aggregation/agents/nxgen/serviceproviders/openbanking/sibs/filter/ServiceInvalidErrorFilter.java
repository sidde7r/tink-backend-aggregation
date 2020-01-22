package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
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
 * HTTP 405 Method not allowed</code>.
 *
 * <p>Typical response body:
 *
 * <p>{"transactionStatus":"RJCT","tppMessages":[{"category":"ERROR","code":"SERVICE_INVALID","text":
 * "The addressed service is not valid for the addressed resources."}]}
 */
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MIN_VALUE)
public class ServiceInvalidErrorFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
            String body = response.getBody(String.class);
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + response.getStatus() + " Error body: " + body);
        }

        return response;
    }
}
