package src.integration.agents.src.main.java.se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;

public class CBIGlobeRetryFilter extends AbstractRandomRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public CBIGlobeRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(
            se.tink.backend.aggregation.nxgen.http.response.HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_BAD_GATEWAY
                || response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                || response.getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT
                || response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE;
    }
}
