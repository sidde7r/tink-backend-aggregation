package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SibsRetryFilter extends AbstractRetryFilter {

    public SibsRetryFilter() {
        super(3, 500);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return 503 == response.getStatus();
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
