package se.tink.backend.aggregation.nxgen.http.filter.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * ServiceUnavailableBankServiceErrorFilter Utility filter to throw a Tink {@link BankServiceError}
 * when an API call responds with <code>
 * HTTP 503 Service Unavailable</code>.
 */
public class ServiceUnavailableBankServiceErrorFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            throw BankServiceError.NO_BANK_SERVICE.exception(
                    "Http status: "
                            + response.getStatus()
                            + ";Response: "
                            + response.getBody(String.class));
        }

        return response;
    }
}
