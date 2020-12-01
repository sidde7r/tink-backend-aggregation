package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class VolvofinansRetryFilter extends AbstractRandomRetryFilter {

    public VolvofinansRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return HttpStatus.SC_BAD_GATEWAY == response.getStatus()
                || HttpStatus.SC_INTERNAL_SERVER_ERROR == response.getStatus();
    }
}
