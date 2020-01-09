package se.tink.backend.aggregation.nxgen.http.filter.filters;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public final class AccessExceededFilter extends Filter {

    @Override
    public HttpResponse handle(final HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        final HttpResponse response = nextFilter(httpRequest);

        // HTTP 429: Too Many Requests
        if (response.getStatus() == 429) {
            String body = response.getBody(String.class).toLowerCase();
            throw BankServiceError.ACCESS_EXCEEDED.exception(
                    "Http status: " + response.getStatus() + " Error body: " + body);
        }

        return response;
    }
}
