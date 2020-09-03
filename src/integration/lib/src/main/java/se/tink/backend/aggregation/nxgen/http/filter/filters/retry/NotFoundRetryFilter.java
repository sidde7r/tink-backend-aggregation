package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/** This filter will back off and retry a given amount of times if the http request return 404 */
public class NotFoundRetryFilter extends AbstractRetryFilter {
    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public NotFoundRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_NOT_FOUND;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
