package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.filter;

import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CollectorRetryFilter extends AbstractRandomRetryFilter {
    public CollectorRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 400
                && response.getBody(String.class).contains(ErrorMessages.ILLEGAL_REQUEST);
    }
}
