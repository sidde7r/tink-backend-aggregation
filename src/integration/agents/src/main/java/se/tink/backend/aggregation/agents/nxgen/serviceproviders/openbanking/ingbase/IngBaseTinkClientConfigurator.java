package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngBaseGatewayTimeoutFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngBaseSignatureInvalidFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngRetryFilter;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

public final class IngBaseTinkClientConfigurator {

    public void configureClient(TinkHttpClient client, int maxRetries, int retrySleepMilliseconds) {
        client.addFilter(new IngRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new TimeoutRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new TerminatedHandshakeRetryFilter(maxRetries, retrySleepMilliseconds));
        client.addFilter(new IngBaseSignatureInvalidFilter());
        client.addFilter(new TimeoutFilter());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new IngBaseGatewayTimeoutFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
    }
}
