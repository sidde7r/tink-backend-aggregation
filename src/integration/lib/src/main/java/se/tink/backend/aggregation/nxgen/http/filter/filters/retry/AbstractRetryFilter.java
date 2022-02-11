package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * An abstract retry filter, that can repeat its operation depending on the result of each request.
 * Operations will be retried at most a certain amount of times with fixed sleep in-between each
 * attempt.
 */
public abstract class AbstractRetryFilter extends Filter {

    protected static final Logger log =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Getter(AccessLevel.PROTECTED)
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

        RuntimeException caughtException = null;

        for (int retryCount = 0; retryCount <= maxNumRetries; retryCount++) {

            try {
                HttpResponse httpResponse = nextFilter(httpRequest);
                if (shouldRetry(httpResponse) && !isLastAttempt(retryCount)) {
                    log.info(
                            "[{}] Filter received retryable response, retrying [{}/{}]",
                            getClassName(),
                            retryCount + 1,
                            maxNumRetries);

                    Uninterruptibles.sleepUninterruptibly(
                            getRetrySleepMilliseconds(retryCount), TimeUnit.MILLISECONDS);

                    continue;
                }

                logFinalRetryResult(httpResponse, retryCount, caughtException);
                return httpResponse;
            } catch (RuntimeException e) {
                if (shouldRetry(e) && !isLastAttempt(retryCount)) {
                    log.info(
                            "[{}] Filter caught retryable exception, retrying [{}/{}]",
                            getClassName(),
                            retryCount + 1,
                            maxNumRetries,
                            e);
                    caughtException = e;
                    continue;
                }
                if (shouldRetry(e)) {
                    log.warn(
                            "[{}] Failed to retry on exception after [{}] retries",
                            getClassName(),
                            retryCount,
                            e);
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
    protected abstract boolean shouldRetry(HttpResponse response);

    /**
     * Informs if the given exception is unacceptable and the operation should be retried. Returns
     * {@code false} by default. Implementations should override this method if they want to retry
     * based on exceptions thrown when execution the filter chain.
     *
     * @param exception the exception to analyze.
     * @return {@code true} if the operation should be retried, {@code false} otherwise.
     */
    protected abstract boolean shouldRetry(RuntimeException exception);

    /**
     * Returns the sleep between retries. Can be overridden for custom logic
     *
     * @param retry the current retry attempt
     * @return the time that should pass before next retry
     */
    protected long getRetrySleepMilliseconds(int retry) {
        return retrySleepMilliseconds;
    }

    private void logFinalRetryResult(
            HttpResponse httpResponse, int retryCount, RuntimeException caughtException) {
        if (shouldRetry(httpResponse)) {
            log.warn(
                    "[{}] Failed to get a successful response after [{}] retries.",
                    getClassName(),
                    retryCount);
        } else if (caughtException != null) {
            log.info(
                    "[{}] Successfully retried on exception after [{}] retries",
                    getClassName(),
                    retryCount,
                    caughtException);
        } else if (retryCount > 0) {
            log.info(
                    "[{}] Successfully retried on httpResponse after [{}] retries.",
                    getClassName(),
                    retryCount);
        }
    }

    private String getClassName() {
        return this.getClass().getSimpleName();
    }
}
