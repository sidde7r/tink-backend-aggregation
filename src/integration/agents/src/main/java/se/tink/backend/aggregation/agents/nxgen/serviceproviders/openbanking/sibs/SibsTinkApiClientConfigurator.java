package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.ConsentInvalidErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.ServiceInvalidErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.SibsAccessExceededErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.SibsBadRequestErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.SibsRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.SibsServiceInvalidRetryFilter;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.RateLimitFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;

public class SibsTinkApiClientConfigurator {

    public void applyFilters(
            TinkHttpClient client,
            SibsRetryFilterProperties sibsRetryFilterProperties,
            SibsRateLimitFilterProperties sibsRateLimitFilterProperties,
            String providerName) {
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new ServiceInvalidErrorFilter());
        client.addFilter(
                new SibsServiceInvalidRetryFilter(
                        sibsRetryFilterProperties.getMaxNumRetries(),
                        sibsRetryFilterProperties.getServiceInvalidRetrySleepMilliseconds()));
        client.addFilter(new ConsentInvalidErrorFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        client.addFilter(new SibsBadRequestErrorFilter());
        client.addFilter(
                new SibsRetryFilter(
                        sibsRetryFilterProperties.getMaxNumRetries(),
                        sibsRetryFilterProperties.getRetrySleepMilliseconds()));
        client.addFilter(
                new RateLimitFilter(
                        providerName,
                        sibsRateLimitFilterProperties.getRetrySleepMillisecondsMin(),
                        sibsRateLimitFilterProperties.getRetrySleepMillisecondsMax(),
                        sibsRateLimitFilterProperties.getNumberOfRetries()));
        client.addFilter(new SibsAccessExceededErrorFilter());
        client.addFilter(new TimeoutFilter());
    }
}
