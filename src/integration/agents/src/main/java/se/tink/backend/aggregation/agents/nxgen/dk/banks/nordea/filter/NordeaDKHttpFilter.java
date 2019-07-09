package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class NordeaDKHttpFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse response = nextFilter(httpRequest);

        throwIfUnavailable(response);

        return response;
    }

    private void throwIfUnavailable(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE
                || response.getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }
}
