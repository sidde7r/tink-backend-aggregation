package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ServerErrorRetryFilter extends AbstractRetryFilter {

    public ServerErrorRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (isServerErrorResponse(response)) {
            log.info(String.format("Retrying for server error code: %d", response.getStatus()));
            log.info(String.format("Error Body: %s", response.getBody(String.class)));
            return true;
        }

        return false;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }

    private boolean isServerErrorResponse(HttpResponse response) {
        return response.getStatus() >= 500 && response.getStatus() <= 599;
    }
}
