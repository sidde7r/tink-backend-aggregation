package se.tink.backend.aggregation.nxgen.agents.proxy;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.PasswordBasedProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ProxyAuthRequiredFilter;

@Slf4j
public final class ProxyConfigurator {

    private static final String PROXY = "Proxy";
    private final AgentsServiceConfiguration agentsServiceConfiguration;

    @Inject
    public ProxyConfigurator(AgentsServiceConfiguration agentsServiceConfiguration) {
        this.agentsServiceConfiguration = agentsServiceConfiguration;
    }

    public TinkHttpClient assignProxyForUser(
            TinkHttpClient client, Provider provider, String userId) {
        try {
            String countryCode = provider.getMarket().toLowerCase();
            String marketProxy = countryCode + PROXY;

            if (this.agentsServiceConfiguration.isFeatureEnabled(marketProxy)) {
                // Setting proxy via TPP
                final PasswordBasedProxyConfiguration proxyConfiguration =
                        this.agentsServiceConfiguration.getCountryProxy(
                                countryCode, userId.hashCode());
                if (isNotEmptyProxyConfiguration(proxyConfiguration)) {
                    log.info(
                            "[PROXY] Setting proxy {} for market {} with username {}",
                            proxyConfiguration.getHost(),
                            countryCode.toUpperCase(),
                            proxyConfiguration.getUsername());

                    client.setProductionProxy(
                            proxyConfiguration.getHost(),
                            proxyConfiguration.getUsername(),
                            proxyConfiguration.getPassword());
                    client.addFilter(new ProxyAuthRequiredFilter());
                } else {
                    log.warn(
                            "[PROXY] Configuration proxy for {} market is unavailable",
                            countryCode.toUpperCase());
                }
            }
        } catch (RuntimeException e) {
            log.error(
                    "[PROXY] Setting proxy for {} finish with fail due to {}",
                    provider.getName(),
                    e.getMessage());
        }
        return client;
    }

    private boolean isNotEmptyProxyConfiguration(
            PasswordBasedProxyConfiguration proxyConfiguration) {
        return StringUtils.isNotEmpty(proxyConfiguration.getUsername())
                && StringUtils.isNotEmpty(proxyConfiguration.getHost())
                && StringUtils.isNotEmpty(proxyConfiguration.getPassword());
    }
}
