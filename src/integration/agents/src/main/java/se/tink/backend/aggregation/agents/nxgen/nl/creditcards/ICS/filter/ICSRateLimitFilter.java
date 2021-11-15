package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter;

import se.tink.backend.aggregation.nxgen.http.filter.filters.RateLimitFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ICSRateLimitFilter extends RateLimitFilter {

    public ICSRateLimitFilter(
            String providerName,
            int retrySleepMillisecondsMin,
            int retrySleepMillisecondsMax,
            int maxRetries) {
        super(providerName, retrySleepMillisecondsMin, retrySleepMillisecondsMax, maxRetries);
    }

    @Override
    protected boolean isRateLimitResponse(HttpResponse response) {
        return response.getStatus() == 429
                && response.hasBody()
                && response.getBody(String.class).toLowerCase().contains("too many requests");
    }
}
