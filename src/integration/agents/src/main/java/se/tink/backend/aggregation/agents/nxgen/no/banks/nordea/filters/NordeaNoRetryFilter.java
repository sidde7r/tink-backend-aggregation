package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.filters;

import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class NordeaNoRetryFilter extends AbstractRandomRetryFilter {

    private static final String RETRY_ERROR = "Backend integration query failed";

    public NordeaNoRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        String body = response.getBody(String.class);
        return response.getStatus() == 500 && body.contains(RETRY_ERROR);
    }
}
