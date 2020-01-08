package se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry;

import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * This filter will back off and retry a given amount of times when an API call responds with <code>
 *  HTTP 429 Too many requests </code>.
 */
public class RateLimitRetryFilter extends AbstractRandomRetryFilter {
    private static final int TOO_MANY_REQUESTS = 429;

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     *     retries.
     */
    public RateLimitRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == TOO_MANY_REQUESTS;
    }
}
