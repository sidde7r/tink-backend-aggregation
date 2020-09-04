package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import java.net.SocketTimeoutException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * This filter will back off and retry a given amount of times if the http request fails because of
 * a SocketTimeoutException.
 */
public class TimeoutRetryFilter extends AbstractRetryFilter {
    private final Class exceptionToRetryOn;

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     *     retries.
     */
    public TimeoutRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
        this.exceptionToRetryOn = SocketTimeoutException.class;
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return false;
    }

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     *     retries.
     */
    public <T extends Exception> TimeoutRetryFilter(
            int maxNumRetries, long retrySleepMilliseconds, Class<T> exceptionToRetryOn) {
        super(maxNumRetries, retrySleepMilliseconds);
        this.exceptionToRetryOn = exceptionToRetryOn;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        // Matches SocketTimeoutException or any eventual subclass in any depth of the causal chain;
        // recursive chains are properly handled.
        return ExceptionUtils.indexOfType(exception, exceptionToRetryOn) >= 0;
    }
}
