package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/** A retry filter that uses backOff provider to calculate sleep times between tries */
public class IngRetryFilter extends AbstractRetryFilter {

    private final BackOffProvider backOffProvider;

    public IngRetryFilter(int maxNumRetries, BackOffProvider backOffProvider) {
        super(maxNumRetries, 0L);
        this.backOffProvider = backOffProvider;
    }

    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 429;
    }

    protected boolean shouldRetry(RuntimeException exception) {
        if (exception instanceof HttpResponseException) {
            HttpResponseException responseException = (HttpResponseException) exception;
            return responseException.getResponse().getStatus() == 429;
        }
        return false;
    }

    @Override
    protected long getRetrySleepMilliseconds(int retry) {
        return backOffProvider.calculate(retry);
    }
}
