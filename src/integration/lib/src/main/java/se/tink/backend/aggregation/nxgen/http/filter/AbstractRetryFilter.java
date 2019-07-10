package se.tink.backend.aggregation.nxgen.http.filter;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

/**
 * An abstract retry filter, that can repeat its operation depending on the result of each request.
 * Operations will be retried at most a certain amount of times with fixed sleep in-between each
 * attempt.
 */
public abstract class AbstractRetryFilter extends Filter {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final int maxNumRetries;
    private final long retrySleepMilliseconds;

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     *     retries.
     */
    public AbstractRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
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
                HttpResponse httpResponse = nextFilter(httpRequest);
                if (shouldRetry(httpResponse) && !isLastAttempt(retryCount)) {
                    log.warn(
                            "Filter received retryable response, retrying [{}/{}]",
                            retryCount + 1,
                            maxNumRetries);
                    continue;
                }
                return httpResponse;
            } catch (HttpClientException e) {

                if (shouldRetry(e) && !isLastAttempt(retryCount)) {
                    log.warn(
                            "Filter caught retryable exception, retrying [{}/{}]",
                            retryCount + 1,
                            maxNumRetries,
                            e);

                    Uninterruptibles.sleepUninterruptibly(
                            retrySleepMilliseconds, TimeUnit.MILLISECONDS);

                    continue;
                }

                throw e;
            }
        }

        // Since the for loop is there to guard against an infinite loop we can instead end up
        // in this state if the terminating condition is incorrectly specified.
        throw new IllegalStateException(
                "This code should not be reached. AbstractRetryFilter::handle should only result in a successful response or HttpClientException.");
    }

    private boolean isLastAttempt(int retryCount) {
        return retryCount >= maxNumRetries;
    }

    /**
     * Informs if the given response is unacceptable and the operation should be retried. Returns
     * {@code false} by default. Implementations should override this method if they want to retry
     * based on the response object.
     *
     * @param response the response to analyze.
     * @return {@code true} if the operation should be retried, {@code false} otherwise.
     */
    protected boolean shouldRetry(HttpResponse response) {
        return false;
    }

    /**
     * Informs if the given exception is unacceptable and the operation should be retried. Returns
     * {@code false} by default. Implementations should override this method if they want to retry
     * based on exceptions thrown when execution the filter chain.
     *
     * @param exception the exception to analyze.
     * @return {@code true} if the operation should be retried, {@code false} otherwise.
     */
    protected boolean shouldRetry(HttpClientException exception) {
        return false;
    }
}
