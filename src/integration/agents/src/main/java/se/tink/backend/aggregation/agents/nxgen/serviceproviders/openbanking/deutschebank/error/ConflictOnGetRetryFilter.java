package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.error;

import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ConflictOnGetRetryFilter extends AbstractRetryFilter {

    public ConflictOnGetRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return ConflictOnGetFilter.isConflictOnGet(response);
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
