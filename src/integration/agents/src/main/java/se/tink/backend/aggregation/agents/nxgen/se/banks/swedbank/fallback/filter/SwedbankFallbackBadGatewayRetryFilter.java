package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SwedbankFallbackBadGatewayRetryFilter extends AbstractRetryFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SwedbankFallbackBadGatewayRetryFilter.class);

    public SwedbankFallbackBadGatewayRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        boolean retry = response.getStatus() == 502;
        if (retry) {
            LOGGER.info("Received HTTP 502 response. Retrying...");
        }
        return retry;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
