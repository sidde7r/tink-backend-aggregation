package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.filters;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants.Errors.SYSTEM_CLOSED;

import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SparebankenVestRetryFilter extends AbstractRandomRetryFilter {

    public SparebankenVestRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 403
                && response.getBody(String.class).contains(SYSTEM_CLOSED);
    }
}
