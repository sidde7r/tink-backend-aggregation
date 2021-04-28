package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class NordeaServerErrorRetryFilter extends AbstractRetryFilter {

    public NordeaServerErrorRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                || response.getStatus() == HttpStatus.SC_BAD_GATEWAY;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
