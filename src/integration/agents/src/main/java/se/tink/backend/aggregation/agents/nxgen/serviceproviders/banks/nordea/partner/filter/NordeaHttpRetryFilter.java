package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/** Retry if we get error 502 or 503 from Nordea (as per their suggestion) */
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = 2)
public class NordeaHttpRetryFilter extends AbstractRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public NordeaHttpRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
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
