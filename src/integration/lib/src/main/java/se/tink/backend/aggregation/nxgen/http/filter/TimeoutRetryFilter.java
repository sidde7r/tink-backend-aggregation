package se.tink.backend.aggregation.nxgen.http.filter;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

/**
 * This filter will back off and retry a given amount of times if the http request fails because of
 * a SocketTimeoutException.
 */
public class TimeoutRetryFilter extends Filter {
    private static final AggregationLogger log = new AggregationLogger(TimeoutRetryFilter.class);

    private final int maxNumRetries;
    private final long retrySleepMilliseconds;

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     *     retries.
     */
    public TimeoutRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        Preconditions.checkArgument(maxNumRetries > 0, "Number of retries has to be positive.");
        Preconditions.checkArgument(
                retrySleepMilliseconds >= 0, "Sleep time between attempts must not be negative.");
        this.maxNumRetries = maxNumRetries;
        this.retrySleepMilliseconds = retrySleepMilliseconds;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        for (int retryCount = 0; retryCount <= maxNumRetries; retryCount++) {

            try {
                return nextFilter(httpRequest);
            } catch (HttpClientException e) {

                if (!isCauseTimeout(e.getCause()) || retryCount >= maxNumRetries) {
                    throw e;
                }

                log.warn(
                        String.format(
                                "TimeoutRetryFilter caught TimeOut exception, retrying [%d/%d].",
                                retryCount + 1, maxNumRetries),
                        e);

                Uninterruptibles.sleepUninterruptibly(
                        retrySleepMilliseconds, TimeUnit.MILLISECONDS);
            }
        }

        // Since the for loop is there to guard against an infinite loop we can instead end up
        // in this state if the terminating condition is incorrectly specified.
        throw new IllegalStateException(
                "This code should not be reached. TimeoutRetryFilter::handle should only result in a successful response or HttpClientException.");
    }

    private boolean isCauseTimeout(Throwable cause) {
        return cause instanceof SocketTimeoutException;
    }
}
