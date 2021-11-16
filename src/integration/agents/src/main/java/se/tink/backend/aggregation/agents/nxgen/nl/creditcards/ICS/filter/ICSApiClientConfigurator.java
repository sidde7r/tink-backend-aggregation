package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;

public class ICSApiClientConfigurator {

    public void applyFilters(
            TinkHttpClient client,
            ICSRetryFilterProperties icsRetryFilterProperties,
            ICSRateLimitFilterProperties icsRateLimitFilterProperties,
            String providerName) {
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(
                new ICSRetryFilter(
                        icsRetryFilterProperties.getMaxNumRetries(),
                        icsRetryFilterProperties.getRetrySleepMilliseconds()));
        client.addFilter(new AccessExceededFilter());
        client.addFilter(
                new ICSRateLimitFilter(
                        providerName,
                        icsRateLimitFilterProperties.getRetrySleepMillisecondsMin(),
                        icsRateLimitFilterProperties.getRetrySleepMillisecondsMax(),
                        icsRateLimitFilterProperties.getNumberOfRetries()));
        client.addFilter(new TimeoutFilter());
        client.addFilter(
                new TerminatedHandshakeRetryFilter(
                        icsRetryFilterProperties.getMaxNumRetries(),
                        icsRetryFilterProperties.getRetrySleepMilliseconds()));
    }
}
