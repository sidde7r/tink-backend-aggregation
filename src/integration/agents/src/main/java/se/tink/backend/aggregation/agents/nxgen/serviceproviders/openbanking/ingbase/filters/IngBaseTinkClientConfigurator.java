package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;

public final class IngBaseTinkClientConfigurator {

    public void configureClient(TinkHttpClient client, int maxRetries, int retrySleepMilliseconds) {
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new IngBaseInstantSepaErrorFilter());
        client.addFilter(new IngRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new TimeoutFilter());
        client.addFilter(new ConnectionTimeoutRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new TerminatedHandshakeRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new IngBaseSignatureInvalidFilter());
        client.addFilter(new IngBaseGatewayTimeoutFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
    }
}
