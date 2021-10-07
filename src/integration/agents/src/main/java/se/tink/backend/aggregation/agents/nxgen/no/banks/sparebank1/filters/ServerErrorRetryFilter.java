package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.filters;

import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ServerErrorRetryFilter extends AbstractRandomRetryFilter {

    private static final int MAX_NUM_RETRIES = 5;
    private static final int RETRY_SLEEP_MILLISECONDS = 1000;

    public ServerErrorRetryFilter() {
        super(MAX_NUM_RETRIES, RETRY_SLEEP_MILLISECONDS);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 500
                && response.hasBody()
                && response.getBody(String.class).contains("ERROR_GENERAL");
    }
}
