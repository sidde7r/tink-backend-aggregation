package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors;

import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class RequestNotProcessedRetryFilter extends AbstractRetryFilter {

    public RequestNotProcessedRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return RequestNotProcessedFilter.isRequestNotProcessedError(response);
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
