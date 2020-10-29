package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.filters;

import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ServerErrorRetryFilter extends AbstractRandomRetryFilter {

    public ServerErrorRetryFilter() {
        super(5, 1000);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 500 || response.getStatus() == 502;
    }
}
