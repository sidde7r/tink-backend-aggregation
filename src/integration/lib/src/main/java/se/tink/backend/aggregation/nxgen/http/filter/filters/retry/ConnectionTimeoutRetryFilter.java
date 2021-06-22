package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/** Retry filter for http errors in {@link TimeoutRetryFilter} */
public class ConnectionTimeoutRetryFilter extends AbstractRetryFilter {

    public ConnectionTimeoutRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        // do not retry when there's no client exception
        return false;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        if (exception instanceof HttpClientException) {
            return TimeoutFilter.isConnectionTimeoutException((HttpClientException) exception);
        }
        return false;
    }
}
