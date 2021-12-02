package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter;

import java.util.Date;
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
            int retrySleepMilliseconds,
            Date sessionExpiryDate) {
        client.addFilter(new ServerErrorFilter());
        client.addFilter(new BelfiusUnknownRefreshTokenErrorLoggingFilter(sessionExpiryDate));
        client.addFilter(new BelfiusTokenErrorFilter(persistentStorage, sessionExpiryDate));
        client.addFilter(new BelfiusConsentErrorFilter(persistentStorage, sessionExpiryDate));
        client.addFilter(new TimeoutFilter());
        client.addFilter(new ConnectionTimeoutRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new TerminatedHandshakeRetryFilter(maxRetries, retrySleepMilliseconds));
    }
}
