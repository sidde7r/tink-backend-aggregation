package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaDKHttpFilter extends AbstractRetryFilter {

    public NordeaDKHttpFilter() {
        super(3, 200);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse response = super.handle(httpRequest);

        // if retried many times and still unavailable, throw exception
        if (isUnavailable(response)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + response.getStatus());
        }

        return response;
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return isUnavailable(response);
    }

    private boolean isUnavailable(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE
                || response.getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT;
    }
}
