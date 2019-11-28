package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.filter.AbstractRetryFilter;

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
}
