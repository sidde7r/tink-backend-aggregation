package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class GatewayTimeoutRetryFilter extends AbstractRetryFilter {

    public GatewayTimeoutRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 504;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
