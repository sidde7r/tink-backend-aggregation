package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class JyskeBankRetryFilter extends AbstractRandomRetryFilter {
    public JyskeBankRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE;
    }
}
