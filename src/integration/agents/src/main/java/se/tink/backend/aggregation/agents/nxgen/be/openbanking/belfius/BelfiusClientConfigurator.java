package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServerErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class BelfiusClientConfigurator {

    public void configure(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            int maxRetries,
            int retrySleepMilliseconds) {
        client.setResponseStatusHandler(new BelfiusResponseStatusHandler(persistentStorage));
        client.addFilter(new ServerErrorFilter());
        client.addFilter(new TimeoutFilter());
        client.addFilter(new ConnectionTimeoutRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new TerminatedHandshakeRetryFilter(maxRetries, retrySleepMilliseconds));
    }
}
