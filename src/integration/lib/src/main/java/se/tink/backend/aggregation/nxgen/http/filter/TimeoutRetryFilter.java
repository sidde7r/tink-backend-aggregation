package se.tink.backend.aggregation.nxgen.http.filter;

import java.net.SocketTimeoutException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;

/**
 * This filter will back off and retry a given amount of times if the http request fails because of
 * a SocketTimeoutException.
 */
public class TimeoutRetryFilter extends AbstractRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     *     retries.
     */
    public TimeoutRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpClientException exception) {
        return exception.getCause() instanceof SocketTimeoutException;
    }
}
