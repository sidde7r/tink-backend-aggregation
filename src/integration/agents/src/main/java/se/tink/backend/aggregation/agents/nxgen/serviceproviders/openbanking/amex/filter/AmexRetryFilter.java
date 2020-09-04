package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.filter;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class AmexRetryFilter extends AbstractRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public AmexRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    public boolean shouldRetry(HttpResponse response) {
        return response.getStatus() >= 500 && Strings.isNullOrEmpty(response.getBody(String.class));
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
