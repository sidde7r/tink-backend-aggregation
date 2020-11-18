package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class IngRetryFilter extends AbstractRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public IngRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 429;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        if (exception instanceof HttpResponseException) {
            HttpResponseException responseException = (HttpResponseException) exception;
            return responseException.getResponse().getStatus() == 429;
        }
        return false;
    }
}
