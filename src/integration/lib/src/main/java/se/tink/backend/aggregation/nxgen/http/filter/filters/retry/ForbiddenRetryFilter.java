package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ForbiddenRetryFilter extends AbstractRetryFilter {

    public ForbiddenRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {

        boolean retry = response.getStatus() == 403;
        if (retry) {
            log.info("Received HTTP 403 response. Retrying...");
        }
        return retry;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
