package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import lombok.With;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServerErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class BelfiusClientConfigurer {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_SLEEP_MILLISECONDS = 3000;

    @With private final int maxRetries;
    @With private final int retrySleepMilliseconds;

    public BelfiusClientConfigurer() {
        this(MAX_RETRIES, RETRY_SLEEP_MILLISECONDS);
    }

    private BelfiusClientConfigurer(int maxRetries, int retrySleepMilliseconds) {
        this.maxRetries = maxRetries;
        this.retrySleepMilliseconds = retrySleepMilliseconds;
    }

    public void configure(TinkHttpClient client, PersistentStorage persistentStorage) {
        client.setResponseStatusHandler(new BelfiusResponseStatusHandler(persistentStorage));
        client.addFilter(new ServerErrorFilter());
        client.addFilter(new TimeoutFilter());
        client.addFilter(new ConnectionTimeoutRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new TerminatedHandshakeRetryFilter(maxRetries, retrySleepMilliseconds));
    }
}
