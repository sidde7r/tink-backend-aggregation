package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

final class AccessExceededFilter extends Filter {

    @Override
    public HttpResponse handle(final HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        final HttpResponse response = nextFilter(httpRequest);

        // HTTP 429: Too Many Requests
        if (response.getStatus() == 429) {
            String body = response.getBody(String.class).toLowerCase();
            if (body.contains(ErrorMessages.ACCESS_EXCEEDED)) {
                throw BankServiceError.ACCESS_EXCEEDED.exception();
            }
        }

        return response;
    }
}
