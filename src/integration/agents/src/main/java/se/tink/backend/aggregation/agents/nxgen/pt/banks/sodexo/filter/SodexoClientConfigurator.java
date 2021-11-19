package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.filter;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;

public final class SodexoClientConfigurator {

    public void applyFilters(TinkHttpClient client, int maxRetries, int retrySleepMilliseconds) {
        client.addFilter(new TimeoutFilter());
        client.addFilter(new ConnectionTimeoutRetryFilter(maxRetries, retrySleepMilliseconds));
    }
}
