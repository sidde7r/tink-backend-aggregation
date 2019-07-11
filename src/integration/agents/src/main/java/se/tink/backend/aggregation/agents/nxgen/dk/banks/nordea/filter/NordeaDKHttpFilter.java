package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.AbstractRetryFilter;

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
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
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
