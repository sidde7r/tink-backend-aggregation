package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class AmericanExpressV62HttpRetryFilter extends AbstractRetryFilter {
    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public AmericanExpressV62HttpRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return HttpStatus.SC_BAD_GATEWAY == response.getStatus()
                || HttpStatus.SC_SERVICE_UNAVAILABLE == response.getStatus();
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
