package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class NordnetFoundRetryFilter extends AbstractRetryFilter {

    public NordnetFoundRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_MOVED_TEMPORARILY;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
