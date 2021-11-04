package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.filter;

import se.tink.backend.aggregation.nxgen.http.filter.filters.RateLimitFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

// Currently Barclays is returning a lot of 502 for our BG refreshes, we believe this is due to
// rate limiting. To test this I had to override isRateLimitResponse method with 502 status.
// Ticket on UKOB service desk with detail: OBSD-26678
public class BarclaysRateLimitFilter extends RateLimitFilter {

    public BarclaysRateLimitFilter(
            String providerName,
            long retrySleepMillisecondsMin,
            long retrySleepMillisecondsMax,
            long maxRetries) {
        super(providerName, retrySleepMillisecondsMin, retrySleepMillisecondsMax, maxRetries);
    }

    @Override
    protected boolean isRateLimitResponse(HttpResponse response) {
        int statusCode = response.getStatus();
        return statusCode == 502;
    }
}
