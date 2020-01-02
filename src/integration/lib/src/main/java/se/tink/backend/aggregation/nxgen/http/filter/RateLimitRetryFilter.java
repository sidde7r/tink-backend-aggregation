package se.tink.backend.aggregation.nxgen.http.filter;

import org.apache.commons.math3.random.RandomDataGenerator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

/**
 * This filter will back off and retry a given amount of times when an API call responds with <code>
 *  HTTP 429 Too many requests </code>.
 */
public class RateLimitRetryFilter extends AbstractRetryFilter {
    private static final int TOO_MANY_REQUESTS = 429;

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param maxRetrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     *     retries.
     * @param isFixedSleepTime If the sleep time is fixed or not. If true, then use
     *     maxRetrySleepMilliseconds as sleep time. Otherwise, the sleep time is a random number
     *     among 0 to maxRetrySleepMilliseconds
     */
    public RateLimitRetryFilter(
            int maxNumRetries, long maxRetrySleepMilliseconds, boolean isFixedSleepTime) {
        super(
                maxNumRetries,
                isFixedSleepTime
                        ? maxRetrySleepMilliseconds
                        : (new RandomDataGenerator().nextLong(0, maxRetrySleepMilliseconds)));
    }

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     *     retries.
     */
    public RateLimitRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        this(maxNumRetries, retrySleepMilliseconds, true);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == TOO_MANY_REQUESTS;
    }
}
