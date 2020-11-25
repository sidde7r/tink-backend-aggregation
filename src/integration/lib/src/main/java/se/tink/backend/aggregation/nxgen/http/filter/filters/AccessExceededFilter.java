package se.tink.backend.aggregation.nxgen.http.filter.filters;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.rate_limit_service.RateLimitService;

public final class AccessExceededFilter extends Filter {
    private final String providerName;

    public AccessExceededFilter(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public HttpResponse handle(final HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        final HttpResponse response = nextFilter(httpRequest);

        // HTTP 429: Too Many Requests
        if (response.getStatus() == 429) {
            String body = response.getBody(String.class).toLowerCase();
            BankServiceException ex =
                    BankServiceError.ACCESS_EXCEEDED.exception(
                            "Http status: " + response.getStatus() + " Error body: " + body);
            RateLimitService.INSTANCE.notifyRateLimitExceeded(providerName, ex);
            throw ex;
        }

        return response;
    }
}
