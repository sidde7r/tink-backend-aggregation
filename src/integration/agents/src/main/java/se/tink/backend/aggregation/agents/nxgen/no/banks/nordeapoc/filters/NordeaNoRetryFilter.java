package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.filters;

import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class NordeaNoRetryFilter extends AbstractRandomRetryFilter {

    private static final String RETRY_ERROR = "Backend integration query failed";

    public NordeaNoRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return (response.getStatus() == 500 && response.getBody(String.class).contains(RETRY_ERROR))
                || (response.getStatus() == 404);
    }
}
